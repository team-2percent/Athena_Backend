package goorm.athena.domain.image.service;

import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ImageQueryService {

    private final ImageRepository imageRepository;

    // 썸네일 이미지 불러오기
    public String getImage(Long imageGroupId) {
        return imageRepository.findFirstImageByImageGroupId(imageGroupId)
                .map(image -> getFullUrl(image.getOriginalUrl()))
                .orElse("");
    }

    // 프로젝트 이미지 불러오기
    // (마크다운 이미지는 제외한다.)
    public List<Image> getProjectImages(Long imageGroupId) {
        return imageRepository.findProjectImagesByImageGroupId(imageGroupId);
    }

    // 이미지 url 리스트 불러오기
    public List<String> getImageUrls(List<Image> images) {
        return images.stream()
                .map(image -> getFullUrl(image.getOriginalUrl()))
                .toList();
    }

    /*
     * Path로 이미지 Full URL 조립
     */
    public String getFullUrl(String path) {
        if(path != null) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String domain = attributes.getRequest().getServerName();
            String scheme = "localhost".equals(domain) ? "http" : "https";
            int port = attributes.getRequest().getServerPort();
            boolean isDefaultPort = port == 80 || port == 443;
            return scheme + "://" + domain + (isDefaultPort ? "" : ":" + port) + path;
        } else {
            return "";
        }
    }
}
