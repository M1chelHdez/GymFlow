package com.gymflow.membership; import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record MembershipPlanRequest(@NotBlank @Size(max=100) String name,@Size(max=500) String description,@Min(1) @Max(3650) int durationValue,@NotNull DurationUnit durationUnit,@NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal price){}
