package goorm.athena.domain.user.service;

import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.user.dto.response.*;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.mapper.UserMapper;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public UserHeaderGetResponse getHeaderById(Long userId){
        User user = getUser(userId);

        String imageUrl = "";
        if(user.getImageGroup() != null){
            imageUrl = imageService.getImage(user.getImageGroup().getId());
        }
        return userMapper.toHeaderGetResponse(user, imageUrl);
    }

    // 내 정보 조회 임시 로직
    @Transactional(readOnly = true)
    public UserGetResponse getUserById(Long userId) {
        User user = getUser(userId);
        String imageUrl = null;
        if(user.getImageGroup() != null && user.getImageGroup().getId() != null) {
            imageUrl = imageService.getImage(user.getImageGroup().getId());
        }

        return userMapper.toGetResponse(user, imageUrl);
    }

    public List<Long> getUserIdAll() {
        return userRepository.findAll().stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    public UserSummaryResponse getUserSummary(Long userId){
        User user = getUser(userId);
        return userMapper.toSummaryResponse(user);
    }
}