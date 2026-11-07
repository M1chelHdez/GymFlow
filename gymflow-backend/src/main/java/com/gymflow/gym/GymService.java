package com.gymflow.gym;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
@Service
public class GymService {
    private final GymRepository repository;
    public GymService(GymRepository repository) { this.repository = repository; }
    @Transactional public GymResponse create(GymRequest request) {
        java.time.ZoneId.of(request.timezone());
        return GymResponse.from(repository.save(new Gym(request.name().trim(), request.timezone(), request.currency())));
    }
    @Transactional(readOnly=true) public List<GymResponse> findAll() { return repository.findAll().stream().map(GymResponse::from).toList(); }
    @Transactional(readOnly=true) public GymResponse findById(UUID id) { return repository.findById(id).map(GymResponse::from).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Gimnasio no encontrado.")); }
}
