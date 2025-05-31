package goorm.athena.domain.user.service;

import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import goorm.athena.domain.user.UserIntegrationTestSupport;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest extends UserIntegrationTestSupport {
    @Test
    @DisplayName("이메일이 이미 존재하면 CustomException(ALREADY_EXIST_USER)을 던진다")
    void 이메일이_이미_존재하면_예외_던짐() {
        // given
        String email = "1231@naver.com";
        UserCreateRequest request = new UserCreateRequest(
                email,
                "password123",
                "nickname");

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(request, null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_EXIST_USER.getErrorMessage());

        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("이메일이 공백이 아니라면 검증 실패")
    void 이메일_공백_유효성_검사_테스트() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "", // 이메일 공백 -> 유효성 실패 예상
                "validPassword123", // 비밀번호는 유효한 값
                "validNickname" // 닉네임도 유효한 값
        );

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email"))).isTrue();
    }

    @Test
    @DisplayName("비밀번호가 공백이 아니라면 검증 실패")
    void 비밀번호_공백_유효성_검사_테스트() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "123@naver.com", // 이메일 공백 -> 유효성 실패 예상
                "", // 비밀번호는 유효한 값
                "validNickname" // 닉네임도 유효한 값
        );

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isTrue();
    }

    @Test
    @DisplayName("닉네임이 공백이 아니라면 검증 실패")
    void 닉네임_공백_유효성_검사_테스트() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "123@naver.com", // 이메일 공백 -> 유효성 실패 예상
                "validPassword123", // 비밀번호는 유효한 값
                "" // 닉네임도 유효한 값
        );

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nickname"))).isTrue();
    }

    @Test
    @DisplayName("null 값이 들어오면 유효성 검증 실패")
    void null_이메일_입력값_검증_테스트() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                null, // null 이메일
                "validPassword",
                "nickname");

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email"))).isTrue();
    }

    @Test
    @DisplayName("null 값이 들어오면 유효성 검증 실패")
    void null_비밀번호_입력값_검증_테스트() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "123@naver.com", // null 이메일
                null,
                "nickname");

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isTrue();
    }

    @Test
    @DisplayName("null 값이 들어오면 유효성 검증 실패")
    void null_닉네임_입력값_검증_테스트() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "123@naver.com", // null 이메일
                "validPassword",
                null);

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nickname"))).isTrue();
    }

    @Test
    @DisplayName("비밀번호의 최소 길이가 유효하지 않으면 검증 실패")
    void 비밀번호_최소_길이_유효성_검사_테스트() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "valid@email.com", // 이메일은 유효한 값
                "pw", // 비밀번호 길이 2 (최소 3) -> 실패
                "validNickname" // 닉네임도 유효한 값
        );

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isTrue();
    }

    @Test
    @DisplayName("닉네임의 길이가 최대 길이를 넘어도 유효하지 않다면 검증 실패")
    void 닉네임_최대_길이_유효성_검사_테스트() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "valid@email.co12312321321321321123213132132132132132131231231231232131231232134213213213213123m", // 이메일은
                                                                                                                   // 유효한
                                                                                                                   // 값
                "pw", // 비밀번호 길이 2 (최소 3) -> 실패
                "validNickname" // 닉네임도 유효한 값
        );

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email"))).isTrue();
    }

    @Test
    @DisplayName("비밀번호의 최대 길이가 유효하지 않으면 검증 실패")
    void 비밀번호_최대_길이_유효성_검사_테스트() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "valid@email.com", // 이메일은 유효한 값
                "pw12321312321321321321312321312213123123213213213123123213213123213213213123213123123123213123213213123213213123123213213"
                        +
                        "12321321321313123213213123213213213213213123123", // 비밀번호 길이 2 (최소 3) -> 실패
                "validNickname" // 닉네임도 유효한 값
        );

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isTrue();
    }

    @Test
    @DisplayName("닉네임이 유효하지 않으면 검증 실패")
    void 닉네임_유효성_검사_테스트() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "valid@email.com", // 이메일은 유효한 값
                "validPassword123", // 비밀번호도 유효한 값
                "" // 닉네임 공백 -> 실패
        );

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nickname"))).isTrue();
    }

    @Test
    @DisplayName("잘못된 이메일 형식은 유효성 검증 실패")
    void 이메일_형식_유효성_검사() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "invalid-email", // '@' 없음
                "validPassword123",
                "validNickname");

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email"))).isTrue();
    }

    @Test
    @DisplayName("이모지를 포함한 닉네임은 유효성 검사 실패")
    void 이모지_이메일_검사() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "test@example.com😀",
                "validPassword",
                "닉네임" // 이모지 포함
        );

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email"))).isTrue();
    }

    @Test
    @DisplayName("이모지를 포함한 닉네임은 유효성 검사 실패")
    void 이모지_비밀번호_검사() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "test@example.com",
                "validPasswor😀d",
                "닉네임" // 이모지 포함
        );

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password"))).isTrue();
    }

    @Test
    @DisplayName("이모지를 포함한 닉네임은 유효성 검사 실패")
    void 이모지_닉네임_검사() {
        // given
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "test@example.com",
                "validPassword",
                "닉네임😀" // 이모지 포함
        );

        // when
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(invalidRequest);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nickname"))).isTrue();
    }
}