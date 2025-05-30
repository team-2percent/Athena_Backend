package goorm.athena.domain.notification.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fcm_token")
@Getter
@NoArgsConstructor
public class FcmToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Builder
    public FcmToken(String token, Long userId){
        this.token = token;
        this.userId = userId;
    }

    public void updateToken(String newToken){
        this.token = newToken;
    }
}
