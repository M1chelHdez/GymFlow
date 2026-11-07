package com.gymflow.membership;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    List<Membership> findAllByGym_IdOrderByCreatedAtDesc(UUID gymId);
    List<Membership> findAllByGym_IdAndMember_IdOrderByCreatedAtDesc(UUID gymId, UUID memberId);
}
