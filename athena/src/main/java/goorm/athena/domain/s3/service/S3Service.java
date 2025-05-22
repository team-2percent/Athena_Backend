package goorm.athena.domain.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String imageBucket;

    // MultipartFiles -> S3 이미지 등록
    public List<ImageCreateRequest> uploadFiles(List<MultipartFile> multipartFiles) {
        List<ImageCreateRequest> imageRequests = new ArrayList<>();

        multipartFiles.forEach(file -> {
            // S3에 저장할 이름 및 메타데이터 설정
            String fileName = createFileName(file.getOriginalFilename());
            validateFileExtension(fileName);                    // 파일 유효성 검증
            String originalUrl = uploadToS3(file, fileName);    // S3 파일 업로드

            // S3 업로드가 성공적으로 된다면, 이미지 생성 요청 DTO 생성
            ImageCreateRequest imageRequest = new ImageCreateRequest(
                    null,    // imageGroupId는 나중에 컨트롤러에서 set
                    fileName,
                    originalUrl,
                    file.getContentType()
            );

            imageRequests.add(imageRequest);
        });

        return imageRequests;
    }

    // S3 파일 업로드
    public String uploadToS3(MultipartFile file, String fileName) {
        ObjectMetadata metadata = new ObjectMetadata();     // 메타데이터 설정
        metadata.setContentType(file.getContentType());     // File type
        metadata.setContentLength(file.getSize());          // File size

        // InputStream 으로 추출 후 S3 업로드
        try(InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(imageBucket, fileName, inputStream, metadata));
            return amazonS3.getUrl(imageBucket, fileName).toString();   // S3 Url return
            // 사이즈 별 이미지 URL 추가 필요
        } catch(IOException e){
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    // S3 이미지 삭제 (파일 이름으로 변경해서 삭제)
    public void deleteFiles(List<String> fileUrls) {
        for (String fileUrl : fileUrls){
            String fileName = extractFileNameFromUrl(fileUrl);
            amazonS3.deleteObject(imageBucket, fileName);
        }
    }

    // 파일마다 고유한 이름 부여 (중복 방지)
    private String createFileName(String fileName){
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    // 파일 형식 유지
    private String getFileExtension(String fileName){
        return fileName.substring(fileName.lastIndexOf("."));
    }

    // 파일 형식 검증
    private void validateFileExtension(String fileName){
        String extension = getFileExtension(fileName).toLowerCase();
        if (!extension.equals(".jpg") && !extension.equals(".png") && !extension.equals(".jpeg")){
            throw new CustomException(ErrorCode.INVALID_IMAGE_EXTENSION);
        }
    }

    // URL에서 파일명 추출 (파일 삭제를 위해)
    private String extractFileNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }


    // 이미지 리사이징

}
