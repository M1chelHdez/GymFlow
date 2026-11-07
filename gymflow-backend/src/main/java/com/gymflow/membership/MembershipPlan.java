package com.gymflow.membership;
import com.gymflow.gym.Gym; import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="membership_plans",uniqueConstraints=@UniqueConstraint(name="uk_plan_gym_name",columnNames={"gym_id","name"}))
public class MembershipPlan {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="gym_id",nullable=false) private Gym gym;
 @Column(nullable=false,length=100) private String name; @Column(length=500) private String description;
 @Column(name="duration_value",nullable=false) private int durationValue;
 @Enumerated(EnumType.STRING) @Column(name="duration_unit",nullable=false,length=10) private DurationUnit durationUnit;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal price;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private PlanStatus status=PlanStatus.ACTIVE;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now(); protected MembershipPlan(){}
 public MembershipPlan(Gym gym,String name,String description,int durationValue,DurationUnit durationUnit,BigDecimal price){this.gym=gym;this.name=name;this.description=description;this.durationValue=durationValue;this.durationUnit=durationUnit;this.price=price;}
 public UUID getId(){return id;} public Gym getGym(){return gym;} public String getName(){return name;} public String getDescription(){return description;} public int getDurationValue(){return durationValue;} public DurationUnit getDurationUnit(){return durationUnit;} public BigDecimal getPrice(){return price;} public PlanStatus getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
}
