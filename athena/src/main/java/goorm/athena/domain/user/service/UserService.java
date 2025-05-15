package goorm.athena.domain.user.service;

import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.request.UserLoginRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.UserCreateResponse;
import goorm.athena.domain.user.dto.response.UserGetResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;
    private final ImageService imageService;

    @Transactional
    public UserCreateResponse createUser(UserCreateRequest request){
        Boolean isExist = userRepository.existsByEmail(request.email());

        if(!isExist) {
            User newUser = User.create(
                    request.email(),
                    passwordEncoder.encode(request.password()),
                    request.nickname()
            );

            User savedUser = userRepository.save(newUser);

            return UserMapper.toCreateResponse(savedUser);
        } else{
            throw new CustomException(ErrorCode.ALREADY_EXIST_USER);
        }
    }

    @Transactional
    public UserUpdateResponse updateUser(ImageGroup userImageGroup, Long userId,
                                         UserUpdateRequest request, MultipartFile image){
        User updateUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        updateUser.update(
                userImageGroup,
                request.email(),
                request.nickname(),
                request.sellerIntroduction());

        imageService.uploadImages(List.of(image), userImageGroup);   // 프로필 이미지 등록
        User savedUser = userRepository.save(updateUser);

        return UserMapper.toUpdateResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public User getUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 내 정보 조회 임시 로직
    @Transactional(readOnly = true)
    public UserGetResponse getUserById(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserMapper.toGetResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId){
        userRepository.deleteById(userId);
    }

    @Transactional
    public UserLoginResponse validateUserCredentials(UserLoginRequest request, HttpServletResponse response){
        User user = userRepository.findByEmail(request.email());
        if(user == null || !passwordEncoder.matches(request.password(), user.getPassword())){
            throw new CustomException(ErrorCode.AUTH_INVALID_LOGIN);
        }

        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getNickname(), user.getRole().name());

        // 토큰 발급 공통 로직
        String refreshTokenValue = tokenService.issueToken(user, response);

        return UserMapper.toLoginResponse(user.getId(), accessToken, refreshTokenValue);
    }
}