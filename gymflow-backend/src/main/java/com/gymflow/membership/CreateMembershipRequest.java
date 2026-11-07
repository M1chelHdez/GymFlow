package com.gymflow.membership;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateMembershipRequest(
    @NotNull UUID memberId,
    @NotNull UUID planId,
    @NotNull LocalDate startDate
) {}
