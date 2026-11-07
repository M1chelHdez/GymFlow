package com.gymflow.gym;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "gyms")
public class Gym {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 60) private String timezone = "America/Mexico_City";
    @Column(nullable = false, length = 3) private String currency = "MXN";
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private GymStatus status = GymStatus.ACTIVE;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    protected Gym() {}
    public Gym(String name, String timezone, String currency) { this.name = name; this.timezone = timezone; this.currency = currency; }
    public UUID getId() { return id; } public String getName() { return name; }
    public String getTimezone() { return timezone; } public String getCurrency() { return currency; }
    public GymStatus getStatus() { return status; } public Instant getCreatedAt() { return createdAt; }
}
