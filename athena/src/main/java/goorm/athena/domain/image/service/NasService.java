package goorm.athena.domain.image.service;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class NasService {
    @Value("${nas.base.path}")
    private String imagePath;

    private static final String IMAGE_FORMAT = "webp";
    private final String IMAGE_DOMAIN = "http://localhost:8080/images";

    private static final Map<String, Dimension> SIZES = Map.of(
            "s", new Dimension(200, 200),
            "m", new Dimension(600, 600),
            "l", new Dimension(1280, 960)
    );  // 추후 이미지 사이즈 조정 필요

    /**
     * [이미지 업로드 Method]
     */
    public List<ImageCreateRequest> saveAll(List<MultipartFile> files, Long imageGroupId) {
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
    public ImageCreateRequest save(MultipartFile file, Long imageGroupId)  {
        validateFileExtension(file.getOriginalFilename());
        String fileName = createFileName();                     // 고유한 파일 이름 생성
        File originalFile = new File(imagePath, fileName);

        // 원본 WebP 파일 저장
        ImmutableImage image = null;
        try {
            image = ImmutableImage.loader().fromStream(file.getInputStream());
            image.output(WebpWriter.DEFAULT, originalFile);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.ORIGIN_IMAGE_UPLOAD_FAILED);
        }


        for (var entry : SIZES.entrySet()) {
            // 리사이징된 파일 이름 지정
            String resizedFileName = entry.getKey() + "_" + fileName;
            Dimension dim = entry.getValue();
            File resizedFile = new File(imagePath, resizedFileName);

            // 리사이즈 후 저장
            try {
                image.fit(dim.width, dim.height).output(WebpWriter.DEFAULT, resizedFile);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.IMAGES_UPLOAD_FAILED);
            }
        }

        String imageUrl = IMAGE_DOMAIN + "/" + fileName;  // 이미지 URL
        return new ImageCreateRequest(imageGroupId, fileName, imageUrl, IMAGE_FORMAT);
    }

    // 파일마다 고유한 이름 부여 (중복 방지)
    private String createFileName(){
        return UUID.randomUUID() + "." + IMAGE_FORMAT;
    }

    // 확장자 추출
    private String getFileExtension(String fileName){
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    // 확장자 검증
    private void validateFileExtension(String fileName){
        String extension = getFileExtension(fileName).toLowerCase();
        if (!List.of("jpg", "jpeg", "png").contains(extension)){
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
