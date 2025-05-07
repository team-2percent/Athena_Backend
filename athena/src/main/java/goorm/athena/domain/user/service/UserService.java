package goorm.athena.domain.user.service;

import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.request.UserLoginRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.UserCreateResponse;
import goorm.athena.domain.user.dto.response.UserGetResponse;
import goorm.athena.domain.user.dto.response.UserLoginResponse;
import goorm.athena.domain.user.dto.response.UserUpdateResponse;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.mapper.UserMapper;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.JwtTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;

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
    public UserUpdateResponse updateUser(Long userId, UserUpdateRequest request){
        User updateUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        updateUser.update(request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname());

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

    public void deleteUser(Long userId){
        userRepository.deleteById(userId);
    }


    public User getUserById2(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public UserLoginResponse validateUserCredentials(UserLoginRequest request){
        User user = userRepository.findByEmail(request.email());
        if(user == null || !passwordEncoder.matches(request.password(), user.getPassword())){
            throw new CustomException(ErrorCode.AUTH_INVALID_LOGIN);
        }

        Role role = user.getRole();

        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getEmail(), role.name());
        String refreshToken = jwtTokenizer.createRefreshToken(user.getId(), user.getEmail(), role.name());

        return UserMapper.toLoginResponse(user.getId(), accessToken, refreshToken);
    }

}
