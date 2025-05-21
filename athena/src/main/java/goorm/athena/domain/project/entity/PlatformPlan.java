package goorm.athena.domain.project.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PlatformPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanName name;

    @Column(nullable = false)
    private double platformFeeRate;

    @Column(nullable = false)
    private double pgFeeRate;

    @Column(nullable = false)
    private double vatRate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder
    public PlatformPlan(PlanName name, double platformFeeRate, double pgFeeRate, double vatRate, String description) {
        this.name = name;
        this.platformFeeRate = platformFeeRate;
        this.pgFeeRate = pgFeeRate;
        this.vatRate = vatRate;
        this.description = description;
    }
}