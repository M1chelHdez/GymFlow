package com.gymflow.membership;

import com.gymflow.gym.GymRepository;
import com.gymflow.member.Member;
import com.gymflow.member.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MembershipService {
    private final MembershipRepository memberships;
    private final MembershipPlanRepository plans;
    private final MemberRepository members;
    private final GymRepository gyms;
    public MembershipService(MembershipRepository memberships, MembershipPlanRepository plans, MemberRepository members, GymRepository gyms) {
        this.memberships = memberships; this.plans = plans; this.members = members; this.gyms = gyms;
    }
    @Transactional(readOnly = true)
    public List<MembershipResponse> findAll(UUID gymId, UUID memberId) {
        if (!gyms.existsById(gymId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gimnasio no encontrado.");
        List<Membership> result = memberId == null ? memberships.findAllByGym_IdOrderByCreatedAtDesc(gymId) : memberships.findAllByGym_IdAndMember_IdOrderByCreatedAtDesc(gymId, memberId);
        return result.stream().map(MembershipResponse::from).toList();
    }
    @Transactional
    public MembershipResponse create(UUID gymId, CreateMembershipRequest request) {
        var gym = gyms.findById(gymId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gimnasio no encontrado."));
        Member member = members.findById(request.memberId()).filter(m -> m.getGym().getId().equals(gymId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El socio no existe en este gimnasio."));
        MembershipPlan plan = plans.findById(request.planId()).filter(p -> p.getGym().getId().equals(gymId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El plan no existe en este gimnasio."));
        if (plan.getStatus() != PlanStatus.ACTIVE) throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede asignar un plan inactivo.");
        LocalDate start = request.startDate();
        LocalDate end = switch (plan.getDurationUnit()) {
            case DAY -> start.plusDays(plan.getDurationValue());
            case WEEK -> start.plusWeeks(plan.getDurationValue());
            case MONTH -> start.plusMonths(plan.getDurationValue());
            case YEAR -> start.plusYears(plan.getDurationValue());
        };
        return MembershipResponse.from(memberships.save(new Membership(gym, member, plan, start, end)));
    }
}
