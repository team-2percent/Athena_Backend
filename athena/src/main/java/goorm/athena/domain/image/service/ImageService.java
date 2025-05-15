package goorm.athena.domain.image.service;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.dto.req.ImageUpdateRequest;
import goorm.athena.domain.image.dto.res.ImageCreateResponse;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.mapper.ImageMapper;
import goorm.athena.domain.image.repository.ImageRepository;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final S3Service s3Service;

    // 이미지 저장 응답 DTO 생성
    public List<ImageCreateResponse> createResponses(ImageGroup imageGroup) {
        List<Image> images = imageRepository.findAllByImageGroup(imageGroup);
        List<ImageCreateResponse> responses = new ArrayList<>();
        for (Image image : images) {
            responses.add(ImageMapper.toCreateDto(image));
        }

        return responses;
    }

    @Transactional
    // 이미지 S3 + DB 저장
    public void uploadImages(List<MultipartFile> files, ImageGroup imageGroup){
        // S3 업로드
        List<ImageCreateRequest> imageCreateRequests = s3Service.uploadFiles(files);
        List<ImageCreateRequest> updatedRequests = imageCreateRequests.stream()
                .map(req -> req.withImageGroupId(imageGroup))
                .toList();                  // 새 DTO 생성: imageGroupId 추가 ver

        List<Image> images = new ArrayList<>();

        for (int i = 0; i < imageCreateRequests.size(); i++) {
            ImageCreateRequest request = updatedRequests.get(i);
            Image image = ImageMapper.toEntity(request, imageGroup);
            if (i == 0){
                image.setAsDefault();       // 썸네일 구분
            }
            images.add(image);
        }
        imageRepository.saveAll(images);   // 이미지 DB 저장
    }

    /*  [단일 이미지 저장]
        파일 이름, URL만 담아 이미지 DB 업로드
        추후 마크다운 로컬 첨부 이미지 처리 시 사용 예정
     */
    public void uploadImage(String fileName, String url){
        ImageCreateRequest request = new ImageCreateRequest(
                null,
                fileName,
                url,
                null
        );
        Image image = ImageMapper.toEntity(request, null);
        imageRepository.save(image);
    }

    /*
        [변경 사항이 있는 이미지만 수정]
        existingUrls: 기존에 존재하는 이미지 중 남아 있는 이미지의 URL
        newImageFiles: 새로 들어온 이미지 파일들
    */
    @Transactional
    public void updateImages(ImageGroup imageGroup,
                             List<ImageUpdateRequest> imageRequests) {
        // 응답 값 URL / File 구분
        List<String> existingUrls = new ArrayList<>();
        List<MultipartFile> newImageFiles = new ArrayList<>();
        for (ImageUpdateRequest imageRequest : imageRequests) {
            if (imageRequest.file() != null){
                newImageFiles.add(imageRequest.file());
            }
            else{
                existingUrls.add(imageRequest.url());
            }
        }

        List<Image> images = getImages(imageGroup);
        List<String> imageUrls = getImageUrls(images);                      // 기존 이미지 Url 리스트

        List<String> removeUrls = compareImages(imageUrls, existingUrls);   // Url 대조 (DB 기준)
        for (Image image : images) {
            if (removeUrls.contains(image.getOriginalUrl())) {
                imageRepository.delete(image);                              // 제거할 URL에 해당되는 파일 제거 (DB)
            }
        }
        s3Service.deleteFiles(removeUrls);                                  // 제거할 URL에 해당되는 파일 제거 (S3)

        if (newImageFiles.isEmpty()) {
            uploadImages(newImageFiles, imageGroup);                        // 새로운 이미지 S3 + DB 저장
        }
    }

    // 저장된 이미지 URL 대조
    private List<String> compareImages(List<String> baseUrls, List<String> existingUrls) {
        List<String> removeUrls = new ArrayList<>();    // 삭제할 URL
        for (String baseUrl : baseUrls) {
            if (!existingUrls.contains(baseUrl)) {
                removeUrls.add(baseUrl);
            }
        }
        return removeUrls;
    }

    // Get Image List
    public List<Image> getImages(ImageGroup imageGroup) {
        return imageRepository.findAllByImageGroup(imageGroup);
    }

    // Get Image url List
    public List<String> getImageUrls(List<Image> images) {
        List<String> imageUrls = new ArrayList<>();
        for (Image image : images) {
            imageUrls.add(image.getOriginalUrl());
        }
        return imageUrls;
    }

    // 연관 이미지 전체 삭제
    @Transactional
    public void deleteImages(ImageGroup imageGroup) {
        List<Image> images = imageRepository.findAllByImageGroup(imageGroup);
        List<String> fileUrls = new ArrayList<>();     // 고유한 파일 이름을 저장할 List
        for (Image image : images) {
            String fileUrl = image.getOriginalUrl();
            fileUrls.add(fileUrl);
        }

        s3Service.deleteFiles(fileUrls);                // S3에서 이미지 삭제
        imageRepository.deleteAll(images);              // DB에서 이미지 삭제
    }

    public String getImage(Long imageGroupId){
        return imageRepository.findFirstImageByImageGroupId(imageGroupId)
                .map(Image::getOriginalUrl)
                .orElse("");

    }

}
