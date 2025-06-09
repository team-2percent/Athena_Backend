package goorm.athena.domain.user.controller;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.UserInfoIntegrationTestSupport;
import goorm.athena.domain.user.dto.request.UserPasswordRequest;
import goorm.athena.domain.user.dto.request.UserUpdatePasswordRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.*;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.LoginUserRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class UserInfoControllerImplTest extends UserInfoIntegrationTestSupport{

    @DisplayName("로그인 한 유저의 소개문구를 리턴한다.")
    @Test
    void getSummary() {
        // given
        ImageGroup userImageGroup = setupUserImageGroup();
        User user = setupUser("test@example.com", "password123", "123", userImageGroup);
        userRepository.save(user);

        LoginUserRequest loginRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);
        UserUpdateRequest request = new UserUpdateRequest("newNickname", "newBio", "imageUrl");

        userService.updateUser(user.getId(), request, null);

        // when
        ResponseEntity<UserSummaryResponse> response = controller.getSummary(loginRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().sellerIntroduction()).isEqualTo(user.getSellerIntroduction());
        assertThat(response.getBody().linkUrl()).isEqualTo(user.getLinkUrl());
    }

    @DisplayName("로그인 한 유저의 작성한 프로젝트 후기 목록을 보여준다.")
    @Test
    void getComments() throws IOException {
        // given
        ImageGroup userImageGroup = setupUserImageGroup();
        User user = setupUser("123", "123", "123", userImageGroup);
        Category category = setupCategory("음식");
        BankAccount bankAccount = setupBankAccount(user, "123" ,"123" ,"123", true);

        userRepository.save(user);
        categoryRepository.save(category);
        bankAccountRepository.save(bankAccount);

        PlatformPlan platformPlan = platformPlanRepository.findById(1L).get();

        // MultipartFile 생성 (임시 WebP 이미지)
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", os); // WebP 포맷 지원 라이브러리 필요

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new ByteArrayInputStream(os.toByteArray())
        );

        MockMultipartFile multipartFile2 = new MockMultipartFile(
                "file2",
                "test2.png",
                "image/png",
                new ByteArrayInputStream(os.toByteArray())
        );

        String markdown = "![sample](test.png)\n![sample2](test2.png)12213132132132";

        ProductRequest productRequest = new ProductRequest("123", "1212", 10000L, 20L, List.of("123", "123"));
        ImageGroup projectImageGroup = setupProjectImageGroup();
        ProjectCreateRequest request = new ProjectCreateRequest(user.getId(), category.getId(), projectImageGroup.getId(), bankAccount.getId(), "123", "12312123312123123",
                10000L, markdown, LocalDateTime.now().plusDays(9), LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(20), platformPlan.getName().name(), List.of(productRequest));

        ProjectIdResponse projectIdResponse = projectService.createProject(request, List.of(multipartFile, multipartFile2));

        Project project = projectRepository.findById(projectIdResponse.projectId()).get();

        Product product = setupProduct(project, "123", "123", 12L, 12L);
        projectRepository.save(project);

        Comment comment = setupComment(user, project, "123");
        Comment comment1 = setupComment(user, project, "123");
        commentRepository.saveAll(List.of(comment, comment1));

        LoginUserRequest loginRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        CommentGetResponse response1 = new CommentGetResponse(
                1L, "123" ,"프로젝2132132131트 제목" ,"123", LocalDateTime.now(), project.getId(), imageService.getImage(project.getImageGroup().getId())
        );

        CommentGetResponse response2 = new CommentGetResponse(
                2L, "123" ,"프로젝2132132131트 제목" ,"123", LocalDateTime.now(), project.getId(), imageService.getImage(project.getImageGroup().getId())
        );

        // when
        ResponseEntity<List<CommentGetResponse>> responses = controller.getComments(loginRequest);

        // then
        assertThat(responses.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responses.getBody().get(0).imageUrl()).isEqualTo(response1.imageUrl());
        assertThat(responses.getBody().get(1).content()).isEqualTo(response2.content());
    }

    @DisplayName("로그인 한 유저가 입력한 비밀번호와 저장된 비밀번호가 서로 같다면 true를 리턴한다.")
    @Test
    void checkPassword() {
        // given
        User user = setupUser("test@example.com", passwordEncoder.encode("password123"), "123", null);
        userRepository.save(user);

        LoginUserRequest loginRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        UserPasswordRequest passwordRequest = new UserPasswordRequest("password123");

        // when
        ResponseEntity<Boolean> response = controller.checkPassword(loginRequest, passwordRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @DisplayName("로그인 한 유저가 입력한 비밀번호와 DB에 저장된 비밀번호가 서로 다르다면 false를 리턴한다.")
    @Test
    void wrongCheckPassword() {
        // given
        User user = setupUser("test@example.com", passwordEncoder.encode("password13"), "123", null);
        userRepository.save(user);

        LoginUserRequest loginRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        UserPasswordRequest passwordRequest = new UserPasswordRequest("password123");

        // when
        ResponseEntity<Boolean> response = controller.checkPassword(loginRequest, passwordRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isFalse();
    }

    @DisplayName("로그인 한 유저가 입력한 비밀번호와 DB에 저장된 비밀번호가 같다면 새 비밀번호로 성공적으로 갱신한다.")
    @Test
    void updatePassword_success() {
        // given
        User user = setupUser("test@example.com", passwordEncoder.encode("password123"), "123", null);
        userRepository.save(user);

        LoginUserRequest request = new LoginUserRequest(user.getNickname(), user.getId(), Role.ROLE_USER);
        UserUpdatePasswordRequest updatePassword = new UserUpdatePasswordRequest("password123", "password12");

        // when
        ResponseEntity<Void> response = controller.updatePassword(request, updatePassword);

        User updatedUser = userRepository.findById(user.getId()).get();
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(passwordEncoder.matches(updatePassword.newPassword(), updatedUser.getPassword())).isTrue();
    }

    @DisplayName("로그인 한 유저가 입력한 비밀번호와 DB에 저장된 비밀번호가 다르다면 에러를 리턴한다.")
    @Test
    void updatePassword_failed() {
        // given
        User user = setupUser("test@example.com", passwordEncoder.encode("password123"), "123", null);
        userRepository.save(user);

        LoginUserRequest request = new LoginUserRequest(user.getNickname(), user.getId(), Role.ROLE_USER);
        UserUpdatePasswordRequest updatePassword = new UserUpdatePasswordRequest("password1234", "password12");

        // when && then
        CustomException exception = assertThrows(CustomException.class, () -> {
            controller.updatePassword(request, updatePassword);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_USER_PASSWORD);
        assertThat(exception.getMessage()).isEqualTo("유저의 비밀번호가 일치하지 않습니다");
    }

    @DisplayName("로그인 한 유저 id와 입력받은 유저 id가 같다면 true를 리턴한다.")
    @Test
    void checkUserId_Success() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);
        LoginUserRequest request = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        User lastUser = userRepository.findAll().getLast();
        // when
        ResponseEntity<Boolean> response = controller.checkUserId(request, lastUser.getId());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @DisplayName("로그인 한 유저 id와 입력받은 유저 id가 같다면 false를 리턴한다.")
    @Test
    void checkUserId_Failed() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);
        LoginUserRequest request = new LoginUserRequest("123", 1L, Role.ROLE_USER);

        // when
        ResponseEntity<Boolean> response = controller.checkUserId(request, 1500L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isFalse();
    }

    @Transactional
    @DisplayName("로그인 한 유저가 프로젝트를 등록했다면, 등록한 프로젝트들을 무한 페이징 형식으로 조회한다.")
    @Test
    void getMyProjects() {
        // given
        ImageGroup imageGroup = setupUserImageGroup();
        ImageGroup projectImageGroup = setupProjectImageGroup();
        ImageGroup projectImageGroup2 = setupProjectImageGroup();
        User user = setupUser("test2@email.com", "1231231", "nickname2", imageGroup);
        Category category = setupCategory("음식");
        BankAccount bankAccount = setupBankAccount(user, "123" ,"123" ,"123", true);
        PlatformPlan platformPlan = platformPlanRepository.findById(1L).get();
        Project project = setupProject(user, category, projectImageGroup, bankAccount, platformPlan,
                "프로젝2132132131트 제목", "설123213213명", 100000L, 10000L, "!23");
        Project project2 = setupProject(user, category, projectImageGroup2, bankAccount, platformPlan,
                "프로젝트 제목123123", "설명123213213", 100000L, 10000L, "!23");

        userRepository.save(user);
        categoryRepository.save(category);
        bankAccountRepository.save(bankAccount);
        platformPlanRepository.save(platformPlan);
        projectRepository.saveAll(List.of(project, project2));

        LoginUserRequest loginRequest = new LoginUserRequest(user.getNickname(), user.getId(), Role.ROLE_USER);
        LocalDateTime nextCursor = LocalDateTime.now();
        Long nextProjectId = project2.getId();
        int pageSize = 10;

        List<MyProjectScrollResponse.ProjectPreview> projectPreviews = List.of(
                new MyProjectScrollResponse.ProjectPreview(
                        project.getId(), project.getTitle(), false, nextCursor.minusDays(5), nextCursor.plusDays(9), 90L, "http://img1.com"
                ),
                new MyProjectScrollResponse.ProjectPreview(
                        project2.getId(), project2.getTitle(), false, nextCursor.minusDays(10), nextCursor.plusDays(15), 100L, "http://img2.com"
                )
        );

        MyProjectScrollResponse expectedResponse = new MyProjectScrollResponse(
                projectPreviews,
                nextCursor,
                nextProjectId
        );

        // when
        ResponseEntity<MyProjectScrollResponse> response =
                controller.getMyProjects(loginRequest, nextCursor, nextProjectId, pageSize);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(expectedResponse.content().getFirst().title()).isEqualTo(response.getBody().content().getLast().title());
        assertThat(expectedResponse.content().getFirst().projectId()).isEqualTo(response.getBody().content().getLast().projectId());
    }

//    @DisplayName("로그인 한 유저가 프로젝트를 구매한 적이 있다면, 구매한 프로젝트들을 무한 페이징 형식으로 조회한다.")
//    @Test
//    void getMyOrders() {
//        // given
//        ImageGroup imageGroup = setupUserImageGroup();
//        ImageGroup projectImageGroup = setupProjectImageGroup();
//        User user = setupUser("test2@email.com", "1231231", "nickname2", imageGroup);
//        Category category = setupCategory("음식");
//        BankAccount bankAccount = setupBankAccount(user, "123" ,"123" ,"123", true);
//        PlatformPlan platformPlan = platformPlanRepository.findById(1L).get();
//        Project project = setupProject(user, category, projectImageGroup, bankAccount, platformPlan,
//                "프로젝2132132131트 제목", "설123213213명", 100000L, 10000L, "!23");
//        DeliveryInfo deliveryInfo = setupDeliveryInfo(user, "12123123", "123123", "123213", true);
//        Product product = setupProduct(project, "123", "123", 12L, 12L);
//        Order order = setupOrder(user, deliveryInfo, project, LocalDateTime.now().minusDays(1));
//        Order order2 = setupOrder(user, deliveryInfo, project, LocalDateTime.now().minusDays(2));
//        Order order3 = setupOrder(user, deliveryInfo, project, LocalDateTime.now().minusDays(2));
//        Order order4 = setupOrder(user, deliveryInfo, project, LocalDateTime.now().minusDays(2));
//
//        OrderItem orderItem1 = setupOrderItem(order, product, 123, 12L);
//        OrderItem orderItem2 = setupOrderItem(order2, product, 123, 123L);
//        OrderItem orderItem3 = setupOrderItem(order3, product, 123, 123L);
//        OrderItem orderItem4 = setupOrderItem(order4, product, 123, 123L);
//
//        userRepository.save(user);
//        categoryRepository.save(category);
//        bankAccountRepository.save(bankAccount);
//        platformPlanRepository.save(platformPlan);
//        projectRepository.save(project);
//        deliveryInfoRepository.save(deliveryInfo);
//        productRepository.save(product);
//        orderRepository.saveAll(List.of(order, order2, order3, order4));
//        orderItemRepository.saveAll(List.of(orderItem1, orderItem2, orderItem3, orderItem4));
//
//
//        LoginUserRequest loginRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);
//        LocalDateTime nextCursor = LocalDateTime.now();
//        Long nextOrderId = orderItem3.getId();
//        int pageSize = 10;
//
//        List<MyOrderScrollResponse.Item> orderItems = List.of(
//                new MyOrderScrollResponse.Item(
//                        orderItem1.getId(), orderItem1.getProduct().getProject().getId(), orderItem1.getProduct().getId(), orderItem1.getProduct().getProject().getTitle(), orderItem1.getProduct().getProductName(), orderItem1.getProduct().getProject().getSeller().getNickname(), "",
//                        nextCursor.minusDays(2), nextCursor.plusDays(10), 75L, true
//                ),
//                new MyOrderScrollResponse.Item(
//                        orderItem2.getId(), orderItem2.getProduct().getProject().getId(), orderItem2.getProduct().getId(), orderItem2.getProduct().getProject().getTitle(), orderItem2.getProduct().getProductName(), orderItem2.getProduct().getProject().getSeller().getNickname(), "",
//                        nextCursor.minusDays(2), nextCursor.plusDays(10), 75L, true
//                ),
//                new MyOrderScrollResponse.Item(
//                        orderItem3.getId(), orderItem3.getProduct().getProject().getId(), orderItem3.getProduct().getId(), orderItem3.getProduct().getProject().getTitle(), orderItem3.getProduct().getProductName(), orderItem3.getProduct().getProject().getSeller().getNickname(), "",
//                        nextCursor.minusDays(1), nextCursor.plusDays(20), 88L, false
//                ),
//                new MyOrderScrollResponse.Item(
//                        orderItem4.getId(), orderItem4.getProduct().getProject().getId(), orderItem4.getProduct().getId(), orderItem4.getProduct().getProject().getTitle(), orderItem4.getProduct().getProductName(), orderItem4.getProduct().getProject().getSeller().getNickname(), "",
//                        nextCursor.minusDays(1), nextCursor.plusDays(20), 88L, false
//                )
//
//        );
//
//        MyOrderScrollResponse expectedResponse = new MyOrderScrollResponse(
//                orderItems,
//                nextCursor.plusDays(1),
//                202L
//        );
//
//        // when
//        ResponseEntity<MyOrderScrollResponse> response =
//                controller.getMyOrders(loginRequest, nextCursor, nextOrderId, pageSize);
//
//        // then
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(expectedResponse.content().getFirst().projectName()).isEqualTo(response.getBody().content().getFirst().projectName());
//        assertThat(expectedResponse.content().getFirst().orderId()).isEqualTo(response.getBody().content().getFirst().orderId());
//    }

        /*
    @DisplayName("로그인 한 유저가 쿠폰들을 발급받았다면, 발급받은 쿠폰들을 무한 페이징 형식으로 조회한다.")
    @Test
    void getUserCoupons_Success() {
        // given
        LoginUserRequest request = new LoginUserRequest("123", 1L, Role.ROLE_USER);

        Long userId = 1L;
        Long cursorId = null;
        int size = 5;

        UserCouponGetResponse coupon1 = new UserCouponGetResponse(
                1L, 101L, "10% 할인 쿠폰", "10% 할인됩니다", 1000, 10,
                LocalDateTime.now().plusDays(7), Status.UNUSED
        );

        UserCouponGetResponse coupon2 = new UserCouponGetResponse(
                2L, 102L, "5000원 할인 쿠폰", "500원 할인됩니다", 5000, 5,
                LocalDateTime.now().plusDays(7), Status.USED
        );

        List<UserCouponGetResponse> couponList = List.of(coupon1, coupon2);
        UserCouponCursorResponse expectedResponse = new UserCouponCursorResponse(couponList, 3L, 10L);

        // when
        when(userCouponService.getUserCoupons(userId, cursorId, size)).thenReturn(expectedResponse);
        ResponseEntity<UserCouponCursorResponse> response = controller.getUserCoupons(request, cursorId, size);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
        verify(userCouponService).getUserCoupons(userId, cursorId, size);
    }

     */
}

