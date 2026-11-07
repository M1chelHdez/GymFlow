package com.gymflow.sale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class SaleServiceTest {
    private JdbcTemplate jdbc;
    private SaleService service;
    private UUID gymId;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        service = new SaleService(jdbc);
        gymId = UUID.randomUUID();
    }

    @Test
    void rejectsSaleWhenGymDoesNotExist() {
        when(jdbc.queryForObject(eq("select count(*) from gyms where id=?"), eq(Integer.class), eq(gymId)))
                .thenReturn(0);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.create(gymId, validPublicSale()));

        assertEquals(404, exception.getStatusCode().value());
        verify(jdbc, never()).update(startsWith("update stock_balances set quantity=quantity-?"), any(Object[].class));
    }

    @Test
    void rejectsMemberThatIsNotActiveOrDoesNotBelongToGym() {
        UUID memberId = UUID.randomUUID();
        when(jdbc.queryForObject(eq("select count(*) from gyms where id=?"), eq(Integer.class), eq(gymId)))
                .thenReturn(1);
        when(jdbc.query(eq("select full_name from members where id=? and gym_id=? and status='ACTIVE'"),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(), eq(memberId), eq(gymId))).thenReturn(List.of());
        CreateSaleRequest request = new CreateSaleRequest(memberId, "CASH",
                List.of(new CreateSaleRequest.SaleLineRequest(UUID.randomUUID(), 1)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.create(gymId, request));

        assertEquals(400, exception.getStatusCode().value());
        verify(jdbc, never()).update(startsWith("update stock_balances set quantity=quantity-?"), any(Object[].class));
    }

    private CreateSaleRequest validPublicSale() {
        return new CreateSaleRequest(null, "CASH",
                List.of(new CreateSaleRequest.SaleLineRequest(UUID.randomUUID(), 1)));
    }
}
