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

### 2. 로기인 & 비밀번호/주민번호 암호화
- 구현여부: 완료 (userId, password로 인증)
- 구현방법
  1) userId, passwor로 로그인 시도
  2) userService의 "authenticateUser"를 통해 사용자 인증
  3) "BCryptPasswordEncoder" 통해 password 암호화 (동일 기능으로 주민번호 암호화)
  4) 로그인 이후 JWT 토큰 생성 (HS512 알고리즘 사용, 유효시간: 1시간으로 설정)
- 검증결과: Test 완료 (API, Test 코드)
