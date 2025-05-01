package goorm.athena.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "user")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(length = 50, nullable = true)
    private String sellerName;

    @Column(length = 3000, nullable = true)
    private String sellerIntroduction;

    @Column(length = 2000, nullable = true)
    private String linkUrl;

    @Builder
    private User(String email, String password, String nickname){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.USER;
    }

    public static User create(String email, String password, String nickname){
        return new User(email, password, nickname);
    }

    public void update(String title, String password, String nickname){
        this.email = title;
        this.password = password;
        this.nickname = nickname;
    }
}