package goorm.athena.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    NOVEL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 Novel이 존재하지 않습니다."),
    PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 Episode의 Page가 존재하지 않습니다."),
    EPISODE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 Episode가 존재하지 않습니다.");

    private final HttpStatus errorCode;
    private final String errorMessage;

    ErrorCode(HttpStatus errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}
