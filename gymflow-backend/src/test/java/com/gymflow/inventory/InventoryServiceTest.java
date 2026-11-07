package com.gymflow.inventory;

import com.gymflow.gym.Gym;
import com.gymflow.gym.GymRepository;
import com.gymflow.product.Product;
import com.gymflow.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryServiceTest {
    private StockBalanceRepository balances;
    private StockMovementRepository movements;
    private ProductRepository products;
    private GymRepository gyms;
    private InventoryService service;
    private UUID gymId;
    private UUID productId;
    private Gym gym;
    private Product product;
    private StockBalance balance;

    @BeforeEach
    void setUp() {
        balances = mock(StockBalanceRepository.class);
        movements = mock(StockMovementRepository.class);
        products = mock(ProductRepository.class);
        gyms = mock(GymRepository.class);
        service = new InventoryService(balances, movements, products, gyms);
        gymId = UUID.randomUUID();
        productId = UUID.randomUUID();
        gym = mock(Gym.class);
        product = mock(Product.class);
        balance = mock(StockBalance.class);

        // El servicio consulta el saldo usando product.getId(); el mock debe devolver el UUID esperado.
        when(product.getId()).thenReturn(productId);
        when(gyms.findById(gymId)).thenReturn(Optional.of(gym));
        when(products.findByIdAndGym_Id(productId, gymId)).thenReturn(Optional.of(product));
        when(balances.findLocked(gymId, productId)).thenReturn(Optional.of(balance));
        // Protección para el flujo en el que el servicio debe crear un saldo nuevo.
        when(balances.saveAndFlush(any(StockBalance.class))).thenReturn(balance);
    }

    @Test
    void addsIncomingStockAndPersistsMovement() {
        when(balance.getQuantity()).thenReturn(4);
        when(movements.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        StockMovementRequest request = new StockMovementRequest(productId, MovementType.IN, 3, "Compra");
        service.move(gymId, request);
        verify(balance).setQuantity(7);
        verify(movements).save(any(StockMovement.class));
    }

    @Test
    void subtractsOutgoingStockWhenEnoughUnitsExist() {
        when(balance.getQuantity()).thenReturn(8);
        when(movements.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        StockMovementRequest request = new StockMovementRequest(productId, MovementType.OUT, 3, "Merma");
        service.move(gymId, request);
        verify(balance).setQuantity(5);
        verify(movements).save(any(StockMovement.class));
    }

    @Test
    void rejectsOutboundMovementWhenStockWouldBecomeNegative() {
        when(balance.getQuantity()).thenReturn(2);
        StockMovementRequest request = new StockMovementRequest(productId, MovementType.OUT, 3, "Stock insuficiente");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.move(gymId, request));
        assertEquals(409, exception.getStatusCode().value());
        verify(balance, never()).setQuantity(anyInt());
        verify(movements, never()).save(any(StockMovement.class));
    }

    @Test
    void rejectsAdjustmentMovement() {
        StockMovementRequest request = new StockMovementRequest(productId, MovementType.ADJUSTMENT, 1, "Ajuste");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.move(gymId, request));
        assertEquals(400, exception.getStatusCode().value());
        verify(balance, never()).setQuantity(anyInt());
        verify(movements, never()).save(any(StockMovement.class));
    }

    @Test
    void rejectsInitialStockWhenBalanceIsAlreadyNonZero() {
        when(balance.getQuantity()).thenReturn(5);
        StockMovementRequest request = new StockMovementRequest(productId, MovementType.INITIAL, 10, "Inicial duplicado");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.move(gymId, request));
        assertEquals(409, exception.getStatusCode().value());
        verify(balance, never()).setQuantity(anyInt());
        verify(movements, never()).save(any(StockMovement.class));
    }

    @Test
    void rejectsMovementWhenGymDoesNotExist() {
        when(gyms.findById(gymId)).thenReturn(Optional.empty());
        StockMovementRequest request = new StockMovementRequest(productId, MovementType.IN, 1, "Entrada");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.move(gymId, request));
        assertEquals(404, exception.getStatusCode().value());
        verifyNoInteractions(products, balances, movements);
    }

    @Test
    void rejectsProductOutsideGym() {
        when(products.findByIdAndGym_Id(productId, gymId)).thenReturn(Optional.empty());
        StockMovementRequest request = new StockMovementRequest(productId, MovementType.IN, 1, "Entrada");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.move(gymId, request));
        assertEquals(404, exception.getStatusCode().value());
        verifyNoInteractions(balances, movements);
    }
}
