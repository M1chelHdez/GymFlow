package com.gymflow.member;
import jakarta.validation.constraints.*;
public record MemberRequest(@NotBlank @Size(max=30) String memberNumber,@NotBlank @Size(max=140) String fullName,@Email @Size(max=180) String email,@Size(max=30) String phone) {}
