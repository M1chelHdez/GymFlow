package com.gymflow.category; import jakarta.validation.constraints.*; public record CategoryRequest(@NotBlank @Size(max=80) String name,@Size(max=300) String description){}
