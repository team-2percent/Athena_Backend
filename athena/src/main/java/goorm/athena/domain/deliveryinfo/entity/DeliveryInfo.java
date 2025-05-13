package goorm.athena.domain.deliveryinfo.entity;

import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "delivery_info")
public class DeliveryInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "address")
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "is_default")
    private boolean isDefault = false;

    public DeliveryInfo(User user, String zipcode, String address, String detailAddress, boolean isDefault) {
        this.user = user;
        this.zipcode = zipcode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.isDefault = isDefault;
    }

    public void updateDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
