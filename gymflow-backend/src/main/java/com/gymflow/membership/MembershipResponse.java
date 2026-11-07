package com.gymflow.membership;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MembershipResponse(UUID id, UUID gymId, UUID memberId, String memberNumber, String memberName,
    UUID planId, String planName, LocalDate startDate, LocalDate endDate, MembershipStatus status,
    BigDecimal agreedPrice, Instant createdAt) {
    static MembershipResponse from(Membership m) {
        return new MembershipResponse(m.getId(), m.getGym().getId(), m.getMember().getId(),
            m.getMember().getMemberNumber(), m.getMember().getFullName(), m.getPlan().getId(), m.getPlan().getName(),
            m.getStartDate(), m.getEndDate(), m.getStatus(), m.getAgreedPrice(), m.getCreatedAt());
    }
}
