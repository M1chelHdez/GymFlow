package com.gymflow.gym;
import java.time.Instant;
import java.util.UUID;
public record GymResponse(UUID id, String name, String timezone, String currency, GymStatus status, Instant createdAt) {
    static GymResponse from(Gym gym) { return new GymResponse(gym.getId(), gym.getName(), gym.getTimezone(), gym.getCurrency(), gym.getStatus(), gym.getCreatedAt()); }
}
