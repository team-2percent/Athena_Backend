package goorm.athena.domain.image.service;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NasService {
    @Value("${nas.base.path}")
    private String imagePath;

    private final String IMAGEDOMAIN = "http://localhost:8080/images";

    private static final Map<String, Dimension> SIZES = Map.of(
            "s", new Dimension(200, 200),
            "m", new Dimension(600, 600),
            "l", new Dimension(1280, 960)
    );  // 추후 이미지 사이즈 조정 필요

    /**
     * [이미지 업로드 Method]
     */
    public List<ImageCreateRequest> saveAll(List<MultipartFile> files, Long imageGroupId) throws IOException {
        List<ImageCreateRequest> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(save(file, imageGroupId));
        }

        return results;
    }

    /*
     *  단일 이미지 저장 method
     *  MultipartFile -> ImageCreateDto
     */
    public ImageCreateRequest save(MultipartFile file, Long imageGroupId) throws IOException {
        String fileName = createFileName(file.getOriginalFilename());                     // 고유한 파일 이름 생성
        validateFileExtension(fileName);
        String extension = getFileExtension(fileName).replace(".", ""); // ex: jpg

        File originalFile = new File(imagePath, fileName);
        file.transferTo(originalFile);                              // 원본 파일 따로 저장

        for (Map.Entry<String, Dimension> entry : SIZES.entrySet()) {
            String sizeKey = entry.getKey();
            String sizedFileName = sizeKey + "_" + fileName;        // 사이즈 별 파일 이름 생성
            File resizedFile = new File(imagePath, sizedFileName);  // 파일 경로 담은 객체 생성

            // 리사이징 후 저장
            Dimension dim = entry.getValue();
            Thumbnails.of(file.getInputStream())
                    .size(dim.width, dim.height)
                    .outputFormat("webp")
                    .toFile(resizedFile);

        }

        String imageUrl = IMAGEDOMAIN + imageGroupId + "/" + fileName + extension;  // 이미지 URL
        return new ImageCreateRequest(imageGroupId, fileName, imageUrl, extension);
    }

    // 파일마다 고유한 이름 부여 (중복 방지)
    private String createFileName(String fileName){
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    // 확장자 추출
    private String getFileExtension(String fileName){
        return fileName.substring(fileName.lastIndexOf("."));
    }

    // 확장자 검증
    private void validateFileExtension(String fileName){
        String extension = getFileExtension(fileName).toLowerCase();
        if (!extension.equals(".jpg") && !extension.equals(".png") && !extension.equals(".jpeg")){
            throw new CustomException(ErrorCode.INVALID_IMAGE_EXTENSION);
        }
    }

    /**
     * [이미지 삭제 Method]
     */
    public void deleteImageFiles(String fileName) {
        // 리사이즈 된 이미지 삭제
        for (String sizeKey : SIZES.keySet()) {
            String sizedFileName = sizeKey + "_" + fileName;
            File file = new File(imagePath, sizedFileName);
            if (file.exists() && !file.delete()) {
                throw new CustomException(ErrorCode.IMAGE_DELETE_FAILED);
            }
        }

        // 원본 파일 삭제
        File originalFile = new File(imagePath, fileName);
        if (originalFile.exists() && !originalFile.delete()) {
            throw new CustomException(ErrorCode.IMAGE_DELETE_FAILED);
        }
    }

}
