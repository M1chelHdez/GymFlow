package com.gymflow.member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MemberRepository extends JpaRepository<Member,UUID> {
 List<Member> findAllByGym_IdOrderByFullName(UUID gymId);
 boolean existsByGym_IdAndMemberNumber(UUID gymId,String memberNumber);
 boolean existsByGym_IdAndMemberNumberAndIdNot(UUID gymId,String memberNumber,UUID id);
}
