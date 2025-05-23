package goorm.athena.domain.image.service;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.mapper.ImageMapper;
import goorm.athena.domain.image.repository.ImageRepository;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageGroupService imageGroupService;
    private final NasService nasService;

    // 다중 이미지 업로드
    @Transactional
    public void uploadImages(List<MultipartFile> files, Long imageGroupId) throws IOException {
        List<ImageCreateRequest> requests = nasService.saveAll(files, imageGroupId);    // NAS에 이미지 저장 및 DTO 반환
        ImageGroup imageGroup = imageGroupService.getById(imageGroupId);

        List<Image> images = new ArrayList<>();
        for(int i = 0; i < requests.size(); i++) {
            ImageCreateRequest request = requests.get(i);
            Image image = ImageMapper.toEntity(request, imageGroup, (long) (i + 1));
            images.add(image);
        }
        imageRepository.saveAll(images);
    }

    // 이미지 전체 삭제
    @Transactional
    public void deleteImages(ImageGroup imageGroup) {
        List<Image> images = imageRepository.findAllByImageGroup(imageGroup);
        for (Image image : images) {
            nasService.deleteImageFiles(image.getFileName());   // 이미지 삭제 (NAS)
        }
        imageRepository.deleteAll(images);                      // 이미지 삭제 (DB)
    }

    ////////////////////////////////////////////////////////////////////////////

    /*  [단일 이미지 저장]
        파일 이름, URL만 담아 이미지 DB 업로드
        추후 마크다운 로컬 첨부 이미지 처리 시 사용 예정

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


        [변경 사항이 있는 이미지만 수정]
        existingUrls: 기존에 존재하는 이미지 중 남아 있는 이미지의 URL
        newImageFiles: 새로 들어온 이미지 파일들

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
    */

    // 썸네일 이미지 불러오기
    public String getImage(Long imageGroupId){
        return imageRepository.findFirstImageByImageGroupId(imageGroupId)
                .map(Image::getOriginalUrl)
                .orElse("");

    }

    // 이미지 리스트 불러오기
    public List<Image> getImages(ImageGroup imageGroup) {
        return imageRepository.findAllByImageGroup(imageGroup);
    }

    // 이미지 url 리스트 불러오기
    public List<String> getImageUrls(List<Image> images) {
        List<String> imageUrls = new ArrayList<>();
        for (Image image : images) {
            imageUrls.add(image.getOriginalUrl());
        }
        return imageUrls;
    }


}
