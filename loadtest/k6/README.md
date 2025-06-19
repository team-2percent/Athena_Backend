# K6 사용 시 필수 팁
아래 명령어와 같이 실행하면 실행 결과를 웹 페이지 형태로 내보내고, 저장할 수 있습니다.
```bash
K6_WEB_DASHBOARD=true K6_WEB_DASHBOARD_EXPORT=html-report.html k6 run script.js
```