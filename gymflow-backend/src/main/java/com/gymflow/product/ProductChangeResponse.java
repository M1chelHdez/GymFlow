package com.gymflow.product;

import java.time.Instant;
import java.util.UUID;

public record ProductChangeResponse(UUID id, String fieldName, String oldValue, String newValue, String changedBy, Instant changedAt) {}
