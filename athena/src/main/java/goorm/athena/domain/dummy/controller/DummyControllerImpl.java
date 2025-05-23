package goorm.athena.domain.dummy.controller;

import goorm.athena.domain.dummy.service.DummyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev")
public class DummyControllerImpl implements DummyController {

    private final DummyService dummyDataService;

    @PostMapping("/dummy-users")
    public ResponseEntity<String> createDummyUsers(@RequestParam(defaultValue = "10") int count) {
        dummyDataService.generateDummyUsers(count);
        return ResponseEntity.ok(count + "개의 더미 유저가 생성되었습니다.");
    }
}