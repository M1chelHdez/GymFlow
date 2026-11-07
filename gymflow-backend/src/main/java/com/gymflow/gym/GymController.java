package com.gymflow.gym;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/gyms")
public class GymController {
    private final GymService service;
    public GymController(GymService service) { this.service = service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public GymResponse create(@Valid @RequestBody GymRequest request) { return service.create(request); }
    @GetMapping public List<GymResponse> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public GymResponse findById(@PathVariable UUID id) { return service.findById(id); }
}
