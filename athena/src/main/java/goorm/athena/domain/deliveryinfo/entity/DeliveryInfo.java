package goorm.athena.domain.deliveryinfo.entity;

import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Column(name = "detail_address", length = 100)
    private String detailAddress;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @Builder
    public DeliveryInfo(User user, String zipcode, String address, String detailAddress, boolean isDefault) {
        this.user = user;
        this.zipcode = zipcode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.isDefault = isDefault;
    }

    public static DeliveryInfo of(User user, DeliveryInfoRequest request) {
        return DeliveryInfo.builder()
                .user(user)
                .zipcode(request.zipcode())
                .address(request.address())
                .detailAddress(request.detailAddress())
                .isDefault(request.isDefault())
                .build();
    }

    public static DeliveryInfo of(User user, DeliveryInfoRequest request, boolean isDefault) {
        return DeliveryInfo.builder()
                .user(user)
                .zipcode(request.zipcode())
                .address(request.address())
                .detailAddress(request.detailAddress())
                .isDefault(isDefault)
                .build();
    }

    public void update(String zipcode, String address, String detailAddress) {
        this.zipcode = zipcode;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    public void updateDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
