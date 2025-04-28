package goorm.athena.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> customExceptionHandler(CustomException ex) {
        log.info("Throw Exception {}", ex.getDescription());
        return ResponseEntity.status(ex.getErrorCode().getErrorCode()).body(ex.getDescription());
    }

}
