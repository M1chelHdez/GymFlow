package com.gymflow.gym;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record GymRequest(@NotBlank @Size(max=120) String name,
                         @NotBlank @Size(max=60) String timezone,
                         @NotBlank @Pattern(regexp="[A-Z]{3}") String currency) {}
