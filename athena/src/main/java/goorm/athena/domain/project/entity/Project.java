package goorm.athena.domain.project.entity;

import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    private Long categoryId;
    private String title;
    private String description;
    private Long goalAmount;
    private Long totalAmount;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime shippedAt;

    @Enumerated(EnumType.STRING)
    private Status status;
}