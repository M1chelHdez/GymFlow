package com.gymflow.sale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

public record CreateSaleRequest(
    UUID memberId,
    @NotNull @Pattern(regexp = "CASH|CARD|TRANSFER|OTHER") String paymentMethod,
    @NotEmpty List<@Valid SaleLineRequest> items
) {
    public record SaleLineRequest(@NotNull UUID productId, @Min(1) int quantity) {}
}
