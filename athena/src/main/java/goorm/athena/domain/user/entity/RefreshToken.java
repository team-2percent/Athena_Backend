package goorm.athena.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name="refresh_token")
@Getter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String value;

    @Builder
    private RefreshToken(User user, String value){
        this.user = user;
        this.value = value;
    }

    public static RefreshToken create(User user, String value){
        return new RefreshToken(user, value);
    }

}
