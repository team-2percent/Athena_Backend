package goorm.athena.domain.user.controller;

import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.user.UserInfoIntegrationTestSupport;
import goorm.athena.domain.user.dto.request.UserPasswordRequest;
import goorm.athena.domain.user.dto.request.UserUpdatePasswordRequest;
import goorm.athena.domain.user.dto.response.*;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.userCoupon.dto.cursor.UserCouponCursorResponse;
import goorm.athena.domain.userCoupon.dto.res.UserCouponGetResponse;
import goorm.athena.domain.userCoupon.entity.Status;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.LoginUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class UserInfoControllerImplTest extends UserInfoIntegrationTestSupport{
    @DisplayName("로그인 한 유저의 소개문구를 리턴한다.")
    @Test
    void getSummary() {
        // given
        UserSummaryResponse expected = new UserSummaryResponse("123", "123");

        // when
        when(userService.getUserSummary(1L)).thenReturn(expected);
        ResponseEntity<UserSummaryResponse> response = controller.getSummary(request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expected, response.getBody());
    }

    @DisplayName("로그인 한 유저의 작성한 프로젝트 후기 목록을 보여준다.")
    @Test
    void getComments() {
        // given
        CommentGetResponse expected1 = new CommentGetResponse(1L, "123", "123", "123", LocalDateTime.now(), 1L, "123");
        CommentGetResponse expected2 = new CommentGetResponse(2L, "12343", "1234", "1234", LocalDateTime.now(), 1L, "1234");

        // when
        when(commentService.getCommentByUser(request.userId())).thenReturn(List.of(expected1, expected2));
        List<CommentGetResponse> response = controller.getComments(request);

        // then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(expected1.id(), response.get(0).id());
        assertEquals(expected2.id(), response.get(1).id());

    }

    @DisplayName("로그인 한 유저가 입력한 비밀번호와 저장된 비밀번호가 서로 같다면 true를 리턴한다.")
    @Test
    void checkPassword() {
        // given
        Long userId = 1L;
        String password = "123123";
        LoginUserRequest request = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        UserPasswordRequest passwordRequest = new UserPasswordRequest("123123");

        // when
        when(userService.checkPassword(1L, passwordRequest.password())).thenReturn(true);
        ResponseEntity<Boolean> response = controller.checkPassword(request, passwordRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());
        verify(userService).checkPassword(1L, password);
    }

    @DisplayName("로그인 한 유저가 입력한 비밀번호와 DB에 저장된 비밀번호가 서로 다르다면 false를 리턴한다.")
    @Test
    void wrongCheckPassword() {
        // given
        String password = "123123123";
        LoginUserRequest request = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        UserPasswordRequest passwordRequest = new UserPasswordRequest(password);

        // when
        when(userService.checkPassword(1L, password)).thenReturn(false);
        ResponseEntity<Boolean> response = controller.checkPassword(request, passwordRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody());
        verify(userService).checkPassword(1L, password);
    }

    @DisplayName("로그인 한 유저가 입력한 비밀번호와 DB에 저장된 비밀번호가 같다면 새 비밀번호로 성공적으로 갱신한다.")
    @Test
    void updatePassword_success() {
        // given
        LoginUserRequest request = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        UserUpdatePasswordRequest updatePassword = new UserUpdatePasswordRequest("oldPassword", "newPassword");

        // when
        doNothing().when(userService).updatePassword(1L, updatePassword);
        ResponseEntity<Void> response = controller.updatePassword(request, updatePassword);

        // then
        assertEquals(204, response.getStatusCodeValue());
        verify(userService).updatePassword(1L, updatePassword);
    }

    @DisplayName("로그인 한 유저가 입력한 비밀번호와 DB에 저장된 비밀번호가 다르다면 에러를 리턴한다.")
    @Test
    void updatePassword_failed() {
        // given
        LoginUserRequest request = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        UserUpdatePasswordRequest updatePassword = new UserUpdatePasswordRequest("oldPassword", "newPassword");

        // when
        doThrow(new CustomException(ErrorCode.INVALID_USER_PASSWORD))
                .when(userService).updatePassword(1L, updatePassword);

        // then
        CustomException exception = assertThrows(CustomException.class,
                () -> controller.updatePassword(request, updatePassword));
        assertEquals(ErrorCode.INVALID_USER_PASSWORD, exception.getErrorCode());
        verify(userService).updatePassword(1L, updatePassword);
    }

    @DisplayName("로그인 한 유저 id와 입력받은 유저 id가 같다면 true를 리턴한다.")
    @Test
    void checkUserId_Success() {
        // given
        LoginUserRequest request = new LoginUserRequest("123", 1L, Role.ROLE_USER);

        // when
        ResponseEntity<Boolean> response = controller.checkUserId(request, 1L);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());

    }

    @DisplayName("로그인 한 유저 id와 입력받은 유저 id가 같다면 false를 리턴한다.")
    @Test
    void checkUserId_Failed() {
        // given
        LoginUserRequest request = new LoginUserRequest("123", 1L, Role.ROLE_USER);

        // when
        ResponseEntity<Boolean> response = controller.checkUserId(request, 2L);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody());

    }

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


    @DisplayName("로그인 한 유저가 프로젝트를 등록했다면, 등록한 프로젝트들을 무한 페이징 형식으로 조회한다.")
    @Test
    void getMyProjects() {
        // given
        LoginUserRequest request = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        LocalDateTime nextCursor = LocalDateTime.now();
        Long nextProjectId = 100L;
        int pageSize = 10;

        List<MyProjectScrollResponse.ProjectPreview> projectPreviews = List.of(
                new MyProjectScrollResponse.ProjectPreview(
                        101L, "Project A", false, nextCursor.minusDays(5), nextCursor.plusDays(10), 90L, "http://img1.com"
                ),
                new MyProjectScrollResponse.ProjectPreview(
                        102L, "Project B", true, nextCursor.minusDays(10), nextCursor.plusDays(5), 100L, "http://img2.com"
                )
        );

        MyProjectScrollResponse expectedResponse = new MyProjectScrollResponse(
                projectPreviews,
                nextCursor.plusDays(1),
                301L
        );

        MyProjectScrollRequest scrollRequest = new MyProjectScrollRequest(nextCursor, nextProjectId, pageSize);

        // when
        when(myInfoService.getMyProjects(1L, scrollRequest)).thenReturn(expectedResponse);
        ResponseEntity<MyProjectScrollResponse> response =
                controller.getMyProjects(request, nextCursor, nextProjectId, pageSize);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
        verify(myInfoService).getMyProjects(1L, scrollRequest);
    }

    @DisplayName("로그인 한 유저가 프로젝트를 구매한 적이 있다면, 구매한 프로젝트들을 무한 페이징 형식으로 조회한다.")
    @Test
    void getMyOrders() {
        // given
        LoginUserRequest request = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        LocalDateTime nextCursor = LocalDateTime.now();
        Long nextOrderId = 200L;
        int pageSize = 10;

        List<MyOrderScrollResponse.Item> orderItems = List.of(
                new MyOrderScrollResponse.Item(
                        1L, 101L, 1001L, "Project A", "Product X", "Seller Alpha", "http://example.com/image1.png",
                        nextCursor.minusDays(2), nextCursor.plusDays(10), 75L, true
                ),
                new MyOrderScrollResponse.Item(
                        2L, 102L, 1002L, "Project B", "Product Y", "Seller Beta", "http://example.com/image2.png",
                        nextCursor.minusDays(1), nextCursor.plusDays(20), 88L, false
                )
        );

        MyOrderScrollResponse expectedResponse = new MyOrderScrollResponse(
                orderItems,
                nextCursor.plusDays(1),
                202L
        );

        MyOrderScrollRequest scrollRequest = new MyOrderScrollRequest(nextCursor, nextOrderId, pageSize);

        // when
        when(myInfoService.getMyOrders(1L, scrollRequest)).thenReturn(expectedResponse);
        ResponseEntity<MyOrderScrollResponse> response =
                controller.getMyOrders(request, nextCursor, nextOrderId, pageSize);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
        verify(myInfoService).getMyOrders(1L, scrollRequest);

    }
}

