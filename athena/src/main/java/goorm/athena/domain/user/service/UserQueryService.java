package goorm.athena.domain.user.service;

import goorm.athena.domain.image.service.ImageQueryService;
import goorm.athena.domain.user.dto.response.*;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.mapper.UserMapper;
import goorm.athena.domain.user.repository.MyInfoQueryRepository;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository userRepository;
    private final ImageQueryService imageQueryService;
    private final MyInfoQueryRepository myInfoQueryRepository;

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
            imageUrl = imageQueryService.getImage(user.getImageGroup().getId());
        }
        return UserMapper.toHeaderGetResponse(user, imageUrl);
    }

    // 내 정보 조회 임시 로직
    @Transactional(readOnly = true)
    public UserGetResponse getUserById(Long userId) {
        User user = getUser(userId);
        String imageUrl = null;
        if(user.getImageGroup() != null && user.getImageGroup().getId() != null) {
            imageUrl = imageQueryService.getImage(user.getImageGroup().getId());
        }

        return UserMapper.toGetResponse(user, imageUrl);
    }

    public List<Long> getUserIdAll() {
        return userRepository.findAll().stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }

    public UserSummaryResponse getUserSummary(Long userId){
        User user = getUser(userId);
        return UserMapper.toSummaryResponse(user);
    }

    public Long getSellerByProjectId(Long projectId){
        User user = myInfoQueryRepository.findSellerByProjectId(projectId);
        return user.getId();
    }
}