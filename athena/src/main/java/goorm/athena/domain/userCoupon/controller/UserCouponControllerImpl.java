package goorm.athena.domain.userCoupon.controller;

import goorm.athena.domain.notification.service.NotificationService;
import goorm.athena.domain.userCoupon.dto.req.UserCouponIssueRequest;
import goorm.athena.domain.userCoupon.dto.req.UserCouponUseRequest;
import goorm.athena.domain.userCoupon.dto.res.UserCouponIssueResponse;
import goorm.athena.domain.userCoupon.scheduler.UserCouponScheduler;
import goorm.athena.domain.userCoupon.service.*;
import goorm.athena.domain.userCoupon.service.test.*;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/userCoupon")
public class UserCouponControllerImpl implements UserCouponController {
    private final UserCouponCommandService userCouponCommandService;
    private final UserCouponQueryService userCouponQueryService;

    private final UserCouponCommandServiceV1 userCouponCommandServiceV1;
    private final UserCouponCommandServiceV2 userCouponCommandServiceV2;
    private final UserCouponCommandServiceV3 userCouponCommandServiceV3;
    private final UserCouponCommandServiceV4 userCouponCommandServiceV4_1; // 현재 단계에서 인자값 오류로 주석 처리
    private final UserCouponCommandServiceV4_2 userCouponCommandServiceV4_2;
    private final UserCouponCommandServiceV4_3 userCouponCommandServiceV4_3;
    private final UserCouponCommandServiceV4_4 userCouponCommandServiceV4_4;
    private final UserCouponCommandServiceV4_5 userCouponCommandServiceV4_5;
    private final UserCouponCommandServiceV4_6 userCouponCommandServiceV4_6;
    private final UserCouponCommandServiceV4_7 userCouponCommandServiceV4_7;
    private final UserCouponCommandServiceV4_8 userCouponCommandServiceV4_8;

    private final UserCouponScheduler userCouponScheduler;
    private final NotificationService notificationService;

    @Override
    @PostMapping
    public ResponseEntity<UserCouponIssueResponse> issueCoupon(@CheckLogin LoginUserRequest loginUserRequest,
                                                               @RequestBody UserCouponIssueRequest request){
        userCouponCommandService.issueCoupon(loginUserRequest.userId(), request);

        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/use")
    public ResponseEntity<Void> useCoupon(@CheckLogin LoginUserRequest loginUserRequest,
                                          @RequestBody UserCouponUseRequest request){
        userCouponCommandService.useCoupon(loginUserRequest.userId(), request.userCouponId());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/scheduler")
    public void schedulerExpiredUserCoupon(){
        userCouponScheduler.expiredUserCoupon();
    }

    // test ----------------------------------------------------------
    // 애플리케이션 락
    @PostMapping("/v1")
    public ResponseEntity<UserCouponIssueResponse> issueCouponWithReentrantLock(@CheckLogin LoginUserRequest loginUserRequest,
                                                               @RequestBody UserCouponIssueRequest request){
        userCouponCommandServiceV1.issueCoupon(loginUserRequest.userId(), request);

        return ResponseEntity.noContent().build();
    }

    // 비관적 락
    @PostMapping("/v2")
    public ResponseEntity<UserCouponIssueResponse> issueCouponWithDBLock(@CheckLogin LoginUserRequest loginUserRequest,
                                                                                @RequestBody UserCouponIssueRequest request){
        userCouponCommandServiceV2.issueCoupon(loginUserRequest.userId(), request);

        return ResponseEntity.noContent().build();
    }

    // Redis 락
    @PostMapping("/v3")
    public ResponseEntity<UserCouponIssueResponse> issueCouponWithRedisLock(@CheckLogin LoginUserRequest loginUserRequest,
                                                                         @RequestBody UserCouponIssueRequest request){
        userCouponCommandServiceV3.issueCoupon(loginUserRequest.userId(), request);

        return ResponseEntity.noContent().build();
    }

    // Redis 락 ( 원자값 )
    @PostMapping("/v4_2")
    public ResponseEntity<UserCouponIssueResponse> issueCouponWithRedisAtomic(@CheckLogin LoginUserRequest loginUserRequest,
                                                                            @RequestBody UserCouponIssueRequest request){
        userCouponCommandServiceV4_2.issueCoupon(loginUserRequest.userId(), request);

        return ResponseEntity.noContent().build();
    }

    // Redis 분산 락
    @PostMapping("/v4_3")
    public ResponseEntity<UserCouponIssueResponse> issueCouponWithRedisDistributedLock(@CheckLogin LoginUserRequest loginUserRequest,
                                                                              @RequestBody UserCouponIssueRequest request){
        userCouponCommandServiceV4_3.issueCoupon(loginUserRequest.userId(), request);

        return ResponseEntity.noContent().build();
    }

    // 동기 기반 Lua Script 사용
    @PostMapping("/v4_4")
    public ResponseEntity<UserCouponIssueResponse> issueCouponWithLuaScript(@CheckLogin LoginUserRequest loginUserRequest,
                                                                                       @RequestBody UserCouponIssueRequest request){
        userCouponCommandServiceV4_4.issueCoupon(loginUserRequest.userId(), request);

        return ResponseEntity.noContent().build();
    }

    // 유사 비동기 기반 Lua Script + Redis Set 사용
    @PostMapping("/v4_5")
    public ResponseEntity<UserCouponIssueResponse> issueCouponWithLuaScriptAsync(@CheckLogin LoginUserRequest loginUserRequest,
                                                                            @RequestBody UserCouponIssueRequest request){
        userCouponCommandServiceV4_5.issueCoupon(loginUserRequest.userId(), request);

        return ResponseEntity.noContent().build();
    }

    /**
     * 이벤트 기반 쿠폰 발급 서비스 (버전 4.6)
     * - Redis에서 재고를 Lua 스크립트로 체크 및 감소
     * - 쿠폰 발급 후 ApplicationEvent를 통해 비동기 DB 저장 및 재고 동기화 처리
     * - 재고 소진 시 별도의 동기화 이벤트 발행 (중복 이벤트 방지 플래그 없음)
     * - 빠른 캐시 처리 및 락 최소화를 목표로 하나, 재고 동기화 및 장애 복구 전략 필요
     */
    @PostMapping("/v4_6")
    public ResponseEntity<UserCouponIssueResponse> issueCouponWithLuaAndJavaCheck(@CheckLogin LoginUserRequest loginUserRequest,
                                                                                 @RequestBody UserCouponIssueRequest request){
        userCouponCommandServiceV4_6.issueCoupon(loginUserRequest.userId(), request);

        return ResponseEntity.noContent().build();
    }

    /**
     * 이벤트 기반 쿠폰 발급 서비스 (버전 4.7)
     * - Redis Lua 스크립트 내에서 재고 체크, 감소 및 품절 플래그 설정까지 처리
     * - 마지막 쿠폰 발급 시 품절 플래그를 Redis에 SETNX로 세팅하여 중복 이벤트 방지
     * - 플래그 TTL(60초)로 중복 동기화 이벤트 발행 방지
     * - 내부에서 재고 상태 및 이벤트 발행을 원자적으로 처리하여 동기화 안정성 향상
     * - 쿠폰 발급 성공 후 ApplicationEventPublisher를 통해 비동기 이벤트(CouponIssueEvent, CouponSyncTriggerEvent)를 발행
     * - 비동기 이벤트 리스너에서 DB 저장 및 재고 동기화 작업을 처리하여 응답 지연 최소화 및 시스템 확장성 확보
     */
    @PostMapping("/v4_7")
    public ResponseEntity<UserCouponIssueResponse> issueCouponWithLuaAndStockAsync(@CheckLogin LoginUserRequest loginUserRequest,
                                                                                  @RequestBody UserCouponIssueRequest request){
        userCouponCommandServiceV4_7.issueCoupon(loginUserRequest.userId(), request);

        return ResponseEntity.noContent().build();
    }

    /**
     * 이벤트 기반 쿠폰 발급 서비스 (버전 4.8)
     * - 4.7 버전의 Redis Lua 스크립트를 활용한 재고 체크 및 품절 플래그 설정 기능 유지
     * - 쿠폰 발급 실패 시 Redis 재고와 발급자 정보(Set) 롤백 기능 추가로 안정성 강화
     * - 롤백 처리 과정에서 Redis Set과 Get이 추가 발생하지만, 부하 높은 상황에 대비한 신뢰성 향상 목적
     * - 발급 성공 시 ApplicationEventPublisher를 통한 비동기 이벤트(CouponIssueEvent, CouponSyncTriggerEvent) 발행 유지
     * - 실패 시 Redis 재고 원복과 발급자 제거 후, 롤백 실패 시 별도의 롤백 이벤트(CouponRollbackRequestEvent) 발행으로 장애 대응 체계 보완
     * - 성능은 소폭 저하되나, 장애 발생 시 데이터 정합성 확보 및 안정적인 동시성 제어를 최우선으로 설계
     */
    @PostMapping("/v4_8")
    public ResponseEntity<UserCouponIssueResponse> issueCouponWithLuaAndStockAsync8(@CheckLogin LoginUserRequest loginUserRequest,
                                                                                   @RequestBody UserCouponIssueRequest request){
        userCouponCommandServiceV4_8.issueCoupon(loginUserRequest.userId(), request);

        return ResponseEntity.noContent().build();
    }

}
