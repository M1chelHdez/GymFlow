package com.gymflow.membership;

import com.gymflow.gym.Gym;
import com.gymflow.member.Member;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "memberships", indexes = {
    @Index(name = "idx_membership_gym_member", columnList = "gym_id, member_id"),
    @Index(name = "idx_membership_gym_end_date", columnList = "gym_id, end_date")
})
public class Membership {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date", nullable = false) private LocalDate endDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private MembershipStatus status = MembershipStatus.ACTIVE;
    @Column(name = "agreed_price", nullable = false, precision = 12, scale = 2) private BigDecimal agreedPrice;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
    protected Membership() {}
    public Membership(Gym gym, Member member, MembershipPlan plan, LocalDate startDate, LocalDate endDate) {
        this.gym = gym; this.member = member; this.plan = plan; this.startDate = startDate; this.endDate = endDate; this.agreedPrice = plan.getPrice();
    }
    public UUID getId() { return id; } public Gym getGym() { return gym; } public Member getMember() { return member; }
    public MembershipPlan getPlan() { return plan; } public LocalDate getStartDate() { return startDate; } public LocalDate getEndDate() { return endDate; }
    public MembershipStatus getStatus() { return status; } public BigDecimal getAgreedPrice() { return agreedPrice; } public Instant getCreatedAt() { return createdAt; }
}
