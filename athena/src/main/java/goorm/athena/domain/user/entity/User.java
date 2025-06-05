package goorm.athena.domain.user.entity;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import jakarta.persistence.*;

import jakarta.validation.constraints.Size;
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

    @Column(length = 50, nullable = false)
    private String email;

    @Size(min = 3, max = 100)
    @Column(length = 100, nullable = false)
    private String password;

    @Size(min = 1, max = 50)
    @Column(length = 50, nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    @Column(length = 200, nullable = true)
    private String sellerIntroduction;

    @Column(length = 1000, nullable = true)
    private String linkUrl;

    // ToDo `createFullUser` 메서드 제거 후 매개변수가 null일 때, 기본값 설정하도록 수정
    @Builder
    private User(ImageGroup imageGroup, String email, String password, String nickname){
        this.imageGroup = imageGroup;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.ROLE_USER;
    }

    public void update(String nickname, String sellerIntroduction, String linkUrl){
        this.nickname = nickname;
        this.sellerIntroduction = sellerIntroduction;
        this.linkUrl = linkUrl;
    }

    public void updatePassword(String password){
        this.password = password;
    }

    // ToDo `createFullUser` 메서드 제거 후 Builder에서 매개변수가 null일 때, 기본값 설정하도록 수정
    public static User createFullUser(ImageGroup imageGroup, String email, String password, String nickname,
                                      Role role, String sellerIntroduction, String linkUrl) {
        User user = new User(imageGroup, email, password, nickname);
        user.role = role;
        user.sellerIntroduction = sellerIntroduction;
        user.linkUrl = linkUrl;
        return user;
    }
}