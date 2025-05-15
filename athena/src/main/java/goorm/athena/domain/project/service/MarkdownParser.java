package goorm.athena.domain.project.service;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MarkdownParser {
    // 정규식으로 마크다운 이미지 태그 parshing
    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[.*?]\\((.*?)\\)");

    /**
     * 마크다운 문자열에서 이미지 파일 경로를 파싱
     * @param markdown 마크다운 원본 텍스트
     * @return 파싱된 이미지 경로 리스트
     */
    public List<String> extractImagePaths(String markdown) {
        List<String> imagePaths = new ArrayList<>();

        Matcher matcher = MARKDOWN_IMAGE_PATTERN.matcher(markdown); // 이미지 태그 parshing
        while (matcher.find()) {
            String imagePath = matcher.group(1);                    // 괄호 안의 내용
            imagePaths.add(imagePath);
        }
        return imagePaths;
    }

    /**
     * 마크다운 내 임시 이미지 경로들을 S3 URL로 치환
     * @param markdown 원본 마크다운
     * @param imagePathToS3Url 이미지 경로 → S3 URL 매핑
     * @return 치환된 마크다운
     */
    public String replaceMarkdown(String markdown, Map<String, String> imagePathToS3Url) {
        Matcher matcher = MARKDOWN_IMAGE_PATTERN.matcher(markdown);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String originalPath = matcher.group(1);                                     // 마크다운에 있는 URL
            String newUrl = imagePathToS3Url.getOrDefault(originalPath, originalPath);  // S3에 없는 이미지 경로일 경우, 유지
            matcher.appendReplacement(buffer, Matcher.quoteReplacement("![](" + newUrl + ")")); // URL 치환
        }

        matcher.appendTail(buffer);     // 마지막 매칭 이후 남아 있는 문자열 전부 추가
        return buffer.toString();
    }

}
