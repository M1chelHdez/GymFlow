package com.gymflow.sale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleResponse(UUID id, String folio, UUID gymId, UUID memberId, String memberName,
        String status, String paymentMethod, String paymentStatus,
        BigDecimal subtotal, BigDecimal discountTotal, BigDecimal total,
        int itemCount, Instant createdAt) {}
