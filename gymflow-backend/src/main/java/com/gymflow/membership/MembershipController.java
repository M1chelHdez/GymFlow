package com.gymflow.membership;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gyms/{gymId}/memberships")
public class MembershipController {
    private final MembershipService service;
    public MembershipController(MembershipService service) { this.service = service; }
    @GetMapping public List<MembershipResponse> findAll(@PathVariable UUID gymId, @RequestParam(required = false) UUID memberId) {
        return service.findAll(gymId, memberId);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public MembershipResponse create(@PathVariable UUID gymId, @Valid @RequestBody CreateMembershipRequest request) {
        return service.create(gymId, request);
    }
}
