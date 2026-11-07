package com.gymflow.member;

import com.gymflow.gym.Gym;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="members", uniqueConstraints=@UniqueConstraint(name="uk_member_gym_number", columnNames={"gym_id","member_number"}), indexes=@Index(name="idx_member_gym_name", columnList="gym_id, full_name"))
public class Member {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="gym_id", nullable=false) private Gym gym;
    @Column(name="member_number", nullable=false, length=30) private String memberNumber;
    @Column(name="full_name", nullable=false, length=140) private String fullName;
    @Column(length=180) private String email;
    @Column(length=30) private String phone;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private MemberStatus status=MemberStatus.ACTIVE;
    @Column(nullable=false, updatable=false) private Instant createdAt=Instant.now();
    protected Member() {}
    public Member(Gym gym,String memberNumber,String fullName,String email,String phone){this.gym=gym;this.memberNumber=memberNumber;this.fullName=fullName;this.email=email;this.phone=phone;}
    public void update(String memberNumber,String fullName,String email,String phone){this.memberNumber=memberNumber;this.fullName=fullName;this.email=email;this.phone=phone;}
    public UUID getId(){return id;} public Gym getGym(){return gym;} public String getMemberNumber(){return memberNumber;} public String getFullName(){return fullName;} public String getEmail(){return email;} public String getPhone(){return phone;} public MemberStatus getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
}
