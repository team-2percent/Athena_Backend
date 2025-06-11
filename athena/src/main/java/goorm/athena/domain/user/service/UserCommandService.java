package goorm.athena.domain.user.service;

import goorm.athena.domain.image.service.ImageCommandService;
import goorm.athena.domain.image.service.ImageQueryService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.request.UserLoginRequest;
import goorm.athena.domain.user.dto.request.UserUpdatePasswordRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.UserCreateResponse;
import goorm.athena.domain.user.dto.response.UserLoginResponse;
import goorm.athena.domain.user.dto.response.UserUpdateResponse;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.mapper.UserMapper;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.JwtTokenizer;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCommandService {
    private final UserRepository userRepository;
    private final ImageService imageQueryService;
    private final PasswordEncoder passwordEncoder;
    private final TokenCommandService tokenCommandService;
    private final JwtTokenizer jwtTokenizer;
    private final UserQueryService userQueryService;

    @Transactional
    public UserCreateResponse createUser(UserCreateRequest request, ImageGroup imageGroup) {
        Boolean isExist = userRepository.existsByEmail(request.email());

        if (!isExist) {
            User newUser = UserMapper.toEntity(request, imageGroup);
            User savedUser = userRepository.save(newUser);

            return UserMapper.toCreateResponse(savedUser);
        } else {
            throw new CustomException(ErrorCode.ALREADY_EXIST_USER);
        }
    }

    @Transactional
    public UserUpdateResponse updateUser(Long userId,
                                         UserUpdateRequest request, MultipartFile file){
        User updateUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        updateUser.update(
                request.nickname(),
                request.sellerIntroduction(),
                request.linkUrl());

        // 프로필 이미지가 들어오는 경우에만 등록
        if(file != null && !file.isEmpty()){
            imageCommandService.uploadImages(List.of(file), updateUser.getImageGroup());
        }

        User savedUser = userRepository.save(updateUser);

        return UserMapper.toUpdateResponse(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public UserLoginResponse validateUserCredentials(UserLoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.email());
        //     if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
        //         throw new CustomException(ErrorCode.AUTH_INVALID_LOGIN);
        //     }

        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getNickname(), user.getRole().name());

        // 토큰 발급 공통 로직
        String refreshTokenValue = tokenCommandService.issueToken(user, response);

        return UserMapper.toLoginResponse(user.getId(), accessToken, refreshTokenValue);
    }

    @Transactional
    public void updatePassword(Long userId, UserUpdatePasswordRequest updatePassword){
        User user = userQueryService.getUser(userId);
        if(checkPassword(userId, updatePassword.oldPassword())){
            user.updatePassword(passwordEncoder.encode(updatePassword.newPassword()));
        }else{
            throw new CustomException(ErrorCode.INVALID_USER_PASSWORD);
        }
    }

    public boolean checkPassword(Long userId, String password){
        User user = userQueryService.getUser(userId);
        return passwordEncoder.matches(password, user.getPassword());
    }
}
