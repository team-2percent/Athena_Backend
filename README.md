# 🛍️ 크라우드 펀딩 E-commerce 프로젝트
크라우드 펀딩 E-commerce 플랫폼인 **텀블벅**의 핵심 요구 사항을 분석하여 만든 서비스입니다. 후원형 전자상거래의 구조적 특징(선주문, 수량제한, 쿠폰 등)을 반영하며, 대규모 트래픽 환경에서도 안정적인 서비스를 제공할 수 있도록 고도화된 구조로 개발되었습니다.  


### ✅ 개발 및 고도화 단계

1. **기획 및 MVP 개발**
   - 크라우드 펀딩의 도메인 특성을 반영한 기능 정의
   - 프로젝트 등록 / 주문 / 결제 / 검색 / 쿠폰 발급 / 알림 등 핵심 기능 구현

2. **단위 · 통합 테스트 및 1차 개선**
   - JUnit5 기반 단위 테스트와 통합 테스트 작성
   - 이미지 최적화 및 마크다운 이미지 처리 방식 개선
   - 쿠폰 발급 및 실시간 알림 동작 안정성 확보

3. **부하 테스트 및 2차 개선**
   - JMeter 및 Signoz 기반 부하 테스트 수행
   - 트래픽 집중 시 병목 구간 분석 및 개선 
   - Redis 적용, 비동기 처리 도입으로 확장성 개선

<br>   

## 🚀 주요 기능 및 개선 사항

🔨 **프로젝트 등록**  
> 이미지 업로드와 함께 프로젝트를 생성하고 상품 옵션까지 구성할 수 있습니다.
> <br> 🔗 [예시) 이미지 최적화](링크달기)

💳 **주문 및 결제**  
> 후원 방식으로 주문을 생성하고 결제를 완료합니다.

🎟️ **선착순 쿠폰 발급**  
> 관리자가 지정한 수량과 조건에 따라, 사용자에게 자동으로 쿠폰이 발급됩니다.

📩 **알림 시스템**  
> 주문, 쿠폰 발급 등 주요 이벤트 발생 시, 사용자에게 실시간 알림을 전송합니다.

🔍 **프로젝트 검색**  
> 키워드, 카테고리  등을 활용한 프로젝트 탐색이 가능합니다.


<br>   


## 🛠️ 기술 스택


| Category         | Stack                                                                 |
|------------------|------------------------------------------------------------------------|
| **Language**     | ![Java](https://img.shields.io/badge/Java-21-007396?logo=java&logoColor=white) |
| **Framework**    | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-6DB33F?logo=springboot) <br> ![Spring Batch](https://img.shields.io/badge/Spring%20Batch-6DB33F?logo=spring&logoColor=white) <br> ![Spring Security](https://img.shields.io/badge/Security-Spring-6DB33F?logo=springsecurity) ![JWT](https://img.shields.io/badge/JWT-0.12.4-blueviolet?logo=jsonwebtokens&logoColor=white) |
| **Database**     | ![MySQL](https://img.shields.io/badge/MySQL-5.7-4479A1?logo=mysql) <br> ![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?logo=redis) <br> ![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?logo=hibernate) <br> ![QueryDSL](https://img.shields.io/badge/QueryDSL-Enabled-4B8BBE) |
| **DevOps**       | ![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
| **CI/CD**        | ![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-Automated-2088FF?logo=githubactions&logoColor=white) |
| **Notification** | ![Firebase](https://img.shields.io/badge/FCM-Firebase-FFCA28?logo=firebase&logoColor=white) |
| **Testing & Monitoring** | ![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?logo=jest&logoColor=white) <br> ![Apache JMeter](https://img.shields.io/badge/JMeter-Performance%20Test-D22128?logo=apachejmeter&logoColor=white) <br> ![Signoz](https://img.shields.io/badge/Signoz-Observability-4B32C3?logo=signoz&logoColor=white) <br> ![JaCoCo](https://img.shields.io/badge/JaCoCo-0.8.11-BB4B4B?logo=codecov&logoColor=white) |
| **Docs**         | ![Swagger](https://img.shields.io/badge/Swagger-2.8.3-85EA2D?logo=swagger&logoColor=black) |

<br>   
