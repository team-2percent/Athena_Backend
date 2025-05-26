package goorm.athena.domain.project.service;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MarkdownParser {
    // 정규식으로 마크다운 이미지 태그 parsing
    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[.*?]\\((.*?)\\)");

    /**
     * 마크다운 문자열에서 이미지 파일 경로를 파싱
     * @param markdown 마크다운 원본 텍스트
     * @return 파싱된 이미지 경로 리스트
     */
    public List<String> extractImagePaths(String markdown) {
        List<String> imagePaths = new ArrayList<>();

        Matcher matcher = MARKDOWN_IMAGE_PATTERN.matcher(markdown); // 이미지 태그 parsing
        while (matcher.find()) {
            String imagePath = matcher.group(1);                    // 괄호 안의 내용
            imagePaths.add(imagePath);
        }
        return imagePaths;
    }

    /**
     * 마크다운 내 임시 이미지 경로들을 URL로 치환
     */
    public String replaceMarkdown(String markdown, List<String> originalPaths, List<String> newUrls) {
        if (originalPaths.size() != newUrls.size()) {
            throw new IllegalArgumentException("마크다운 이미지 개수와 업로드된 이미지 개수가 다릅니다.");
        }

        String convertedMarkdown = markdown;
        for (int i = 0; i < originalPaths.size(); i++) {
            String oldUrl = originalPaths.get(i);
            String newUrl = newUrls.get(i);
            convertedMarkdown = convertedMarkdown.replace("](" + oldUrl + ")", "](" + newUrl + ")");
        }

        return convertedMarkdown;
    }

}
