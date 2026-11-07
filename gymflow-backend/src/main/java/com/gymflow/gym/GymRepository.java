package com.gymflow.gym;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface GymRepository extends JpaRepository<Gym, UUID> {}
