package com.gymflow.member;
import java.time.Instant; import java.util.UUID;
public record MemberResponse(UUID id,UUID gymId,String memberNumber,String fullName,String email,String phone,MemberStatus status,Instant createdAt){static MemberResponse from(Member m){return new MemberResponse(m.getId(),m.getGym().getId(),m.getMemberNumber(),m.getFullName(),m.getEmail(),m.getPhone(),m.getStatus(),m.getCreatedAt());}}
