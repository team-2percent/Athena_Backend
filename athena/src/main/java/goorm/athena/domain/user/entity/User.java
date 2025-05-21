package goorm.athena.domain.user.entity;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import jakarta.persistence.*;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "\"user\"")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_group_id", nullable = true)
    private ImageGroup imageGroup;  // 이미지 그룹 ID

    @Column(length = 255, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    @Column(length = 3000, nullable = true)
    private String sellerIntroduction;

    @Column(length = 2000, nullable = true)
    private String linkUrl;

    @Builder
    private User(String email, String password, String nickname){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.ROLE_USER;
    }

    public static User create(String email, String password, String nickname){
        return new User(email, password, nickname);
    }

    public void update(ImageGroup imageGroup, String nickname, String sellerIntroduction, String linkUrl){
        this.imageGroup = imageGroup;
        this.nickname = nickname;
        this.sellerIntroduction = sellerIntroduction;
        this.linkUrl = linkUrl;
    }

    public void updatePassword(String password){
        this.password = password;
    }

    public void update(String email, String password, String nickname){
        if(email != null){
            this.email = email;
        }
        if(nickname != null){
            this.nickname = nickname;
        }
    }
}