# 삼쩜삼 Backend Test 과제 (지원자 김용수)

### Swagger: http://localhost:8080/swagger-ui/index.html

## 요구 사항
### 1. 회원가입 (특정 회원만 가입, 비밀번호/주민번호 암호화)
- 구현여부: 부분 완료 (비밀번호/주민번호 암호화 추후 진행)
- 구현방법
  1) "WebSecurityConfigurerAdapter" 기능이 Spring 3.X Ver. 부터 사용 불가능
  2) "SecurityFilterChain' 으로 대체해서 진행
  3) 구현한 API, Swagger 인증 없이 진행 하도록 구현
  4) 특정 회원 아닐 시 "등록이 허용되지 않은 사용자 입니다." 문구 표기
- 검증결과: Test 완료 (API, Test 코드)

### 2. 로그인 기능, 비밀번호/주민번호 암호화
- 구현여부: 완료 (userId, password로 인증)
- 구현방법
  1) userId, passwor로 로그인 시도
  2) userService의 "authenticateUser"를 통해 사용자 인증
  3) "BCryptPasswordEncoder" 통해 password 암호화 (동일 기능으로 주민번호 암호화)
  4) 로그인 이후 JWT 토큰 생성 (HS512 알고리즘 사용, 유효시간: 1시간으로 설정)
- 검증결과: Test 완료 (API, Test 코드)

### 3. 가입한 회원정보 조회 기능
- 구현여부: 완료
- 구현방법
  1) JWT Token 인증을 통한 내정보 조회
  2) 주민번호, 패스워드 암호화로 미 표기
- 문제해결 (swagger에서 jwt 토큰 인식 불가능한 현상)  
  1) 원인: swagger에 jwttoken 헤더 인식이 계속 안됌
  2) 분석
    - Bearer Token 형식 미지정: Swagger 구성에서 "Bearer" 토큰 형식을 명확하게 지정하지 않음
    - Security Requirement 미적용: Security Requirement를 OpenAPI 구성에 추가하지 않음
  3) 해결방법
    - Bearer Token 형식 지정: .scheme(BEARER_TOKEN_PREFIX)를 통해 토큰의 스키마로 "Bearer"를 명확하게 지정 하여 Swagger UI에서 "Bearer" 토큰을 사용할 수 있게함
    - Security Requirement 추가: SecurityRequirement 객체를 사용하여 정의된 Security Scheme을 OpenAPI 구성에 추가하여 API 요청 시 인증 정보가 필요함을 나타내며, Swagger UI에서 모든 API 엔드포인트에 대해 해당 Security Scheme 사용 하도록 함
 
### 4. 인증 된 회원 Scrap 기능
- 구현여부: 완료
- 구현방법: JWT Token을 통한 인증 후 userId, password로 외부 데이터 scrap
- 문제해결
  1) 실뢰할 수 없는 코드로 오류 발생: SslUtil class에 getRestTemplateWithoutSslVerification 메서드를 통해 우회 설정
  2) Read timed out 오류 발생: SslUtil class에 1분 time out 설정
  3) DB 저장 할 때 길이, 데이터 종류  문제로 오류: ScrapData class에 길이(65535), 데이터 종류 정의 “Text”로 지정

### 5. 유저의 스크랩 정보를 바탕으로 유저의 결정세액, 퇴직연금세액공제금액 계산
- 구현여부: 완료
- 구현방법
  1) TaxCalculator class를 사용하여 유저의 스크랩 정보에 기반한 세액 계산 로직 구현
  2) 계산된 결과값은 NumberFormat 메소드를 사용하여 가독성 좋게 포맷팅 (소수점 미표기 설정)

