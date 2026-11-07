package com.gymflow.sale;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateSaleRequestTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void acceptsPublicGeneralSaleWithValidItems() {
        CreateSaleRequest request = new CreateSaleRequest(
                null,
                "CASH",
                List.of(new CreateSaleRequest.SaleLineRequest(UUID.randomUUID(), 1))
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsUnsupportedPaymentMethodAndEmptyItems() {
        CreateSaleRequest request = new CreateSaleRequest(null, "CRYPTO", List.of());

        Set<String> invalidProperties = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(invalidProperties.contains("paymentMethod"));
        assertTrue(invalidProperties.contains("items"));
    }

    @Test
    void rejectsMissingProductAndNonPositiveQuantity() {
        CreateSaleRequest request = new CreateSaleRequest(
                null,
                "CARD",
                List.of(new CreateSaleRequest.SaleLineRequest(null, 0))
        );

        assertFalse(validator.validate(request).isEmpty());
        Set<String> invalidProperties = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
        assertTrue(invalidProperties.contains("items[0].productId"));
        assertTrue(invalidProperties.contains("items[0].quantity"));
    }
}
