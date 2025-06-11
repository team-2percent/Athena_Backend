package goorm.athena.util;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.userCoupon.entity.Status;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public class TestEntityFactory {

    public static User createUser(String email, String password, String nickname, ImageGroup imageGroup, Role role) {
        User user = User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .imageGroup(imageGroup)
                .build();

        // Role 리플렉터로 관리자 권한 필요시 직접 주입
        ReflectionTestUtils.setField(user, "role", role);
        return user;
    }

    public static Category createCategory(String categoryName) {
        Category category = new Category();
        ReflectionTestUtils.setField(category, "categoryName", categoryName);
        return category;
    }

    public static BankAccount createBankAccount(User user, String number, String holder, String bankName, boolean isDefault) {
        return BankAccount.builder()
                .user(user)
                .accountNumber(number)
                .accountHolder(holder)
                .bankName(bankName)
                .isDefault(isDefault)
                .build();
    }

    public static PlatformPlan createPlatformPlan(PlanName name, int platformFeeRate, int pgFeeRate, int vatRate, String description) {
        return PlatformPlan.builder()
                .name(name)
                .platformFeeRate(platformFeeRate)
                .pgFeeRate(pgFeeRate)
                .vatRate(vatRate)
                .description(description)
                .build();
    }

    public static Project createProject(User user, Category category, ImageGroup imageGroup,
                                        BankAccount bankAccount, PlatformPlan platformPlan,
                                        String title, String description, long goalAmount, long totalAmount,
                                        String contentMarkDown) {
        return Project.builder()
                .seller(user)
                .category(category)
                .imageGroup(imageGroup)
                .bankAccount(bankAccount)
                .platformPlan(platformPlan)
                .title(title)
                .description(description)
                .goalAmount(goalAmount)
                .totalAmount(totalAmount)
                .contentMarkdown(contentMarkDown)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(30))
                .shippedAt(LocalDateTime.now().plusDays(35))
                .build();
    }

    public static Product createProduct(Project project, String name, String description, Long price, Long stock){
        return Product.builder()
                .project(project)
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .build();
    }

    public static DeliveryInfo createDeliveryInfo(User user, String zipcode, String address, String detailAddress, boolean isDefault){
        return DeliveryInfo.builder()
                .user(user)
                .zipcode(zipcode)
                .address(address)
                .detailAddress(detailAddress)
                .isDefault(isDefault).
                build();
    }

    public static OrderItem createOrderItem(Order order, Product product, int quantity, Long price){
        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(price)
                .build();
    }

    public static Order createOrder(User user, DeliveryInfo delivery, Project project, LocalDateTime orderedAt) {
        return Order.create(user, delivery, project, orderedAt);
    }

    public static Comment createComment(User user, Project project, String content){
        return Comment.builder()
                .user(user)
                .project(project)
                .content(content)
                .build();
    }

    public static Coupon createCoupon(String title, String content, int price, LocalDateTime startAt,
                                      LocalDateTime endAt, LocalDateTime expiresAt, int stock, CouponStatus couponStatus){
        CouponCreateRequest request = new CouponCreateRequest(title, content, price, startAt, endAt, expiresAt, stock);

        Coupon coupon =  Coupon.builder()
                .request(request)
                .build();

        // Coupon 리플렉터로 상태 필요시 직접 주입
        ReflectionTestUtils.setField(coupon, "couponStatus", couponStatus);
        return coupon;
    }

    public static UserCoupon createUserCoupon(User user, Coupon coupon, Status status) {
        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .build();

        // UserCoupon 리플렉터로 상태 필요시 직접 주입
        ReflectionTestUtils.setField(userCoupon, "status", status);
        return userCoupon;
    }
}