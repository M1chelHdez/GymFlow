package com.gymflow.membership; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan,UUID>{List<MembershipPlan> findAllByGym_IdOrderByName(UUID gymId);boolean existsByGym_IdAndNameIgnoreCase(UUID gymId,String name);}
