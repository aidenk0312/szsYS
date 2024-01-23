package szs.YS.user.controller;

import io.jsonwebtoken.JwtException;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import szs.YS.user.entity.ScrapData;
import szs.YS.user.entity.User;
import szs.YS.user.model.LoginDTO;
import szs.YS.user.model.ScrapRequest;
import szs.YS.user.model.UserDTO;
import szs.YS.user.repository.ScrapDataRepository;
import szs.YS.user.service.TaxCalculator;
import szs.YS.user.util.SslUtil;
import szs.YS.user.service.UserService;
import szs.YS.user.service.JwtTokenService;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/szs")
public class UserController {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    @Autowired
    private ScrapDataRepository scrapDataRepository;

    public UserController(UserService userService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/signup")
    @ApiOperation(value = "회원 가입", notes = "사용자를 새로 등록합니다.")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDto) {
        try {
            User user = userService.registerUser(userDto.getUserId(), userDto.getPassword(), userDto.getName(), userDto.getRegNo());
            return ResponseEntity.ok().body("회원 가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @ApiOperation(value = "로그인", notes = "사용자 로그인 및 토큰 발급")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDto) {
        Optional<User> user = userService.authenticateUser(loginDto.getUserId(), loginDto.getPassword());
        if (user.isPresent()) {
            String token = jwtTokenService.generateToken(user.get().getUserId());
            return ResponseEntity.ok().body(token);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {

        String token = request.getHeader("Authorization");
        log.info("Received token: {}", token);

        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Token is missing or incorrect format");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 없거나 형식이 맞지 않습니다.");
        }

        try {
            String userId = jwtTokenService.extractUserId(token.substring(7));
            Optional<User> user = userService.findByUserId(userId);
            if (user.isPresent()) {
                return ResponseEntity.ok().body(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
            }
        } catch (JwtException e) {
            log.error("Token validation error", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 유효하지 않습니다.");
        } catch (Exception e) {
            log.error("Internal server error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("토큰 검증 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/scrap")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", required = true, dataType = "string", paramType = "header")
    })
    public ResponseEntity<?> scrapUserData(@RequestBody ScrapRequest requestModel, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 토큰이 필요합니다.");
        }

        String userId = jwtTokenService.extractUserId(token.substring(7));
        Optional<User> user = userService.findByUserId(userId);
        if (user.isPresent()) {
            User currentUser = user.get();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ScrapRequest> requestEntity = new HttpEntity<>(requestModel, headers);

            RestTemplate restTemplate = SslUtil.getRestTemplateWithoutSslVerification();
            ResponseEntity<String> response = restTemplate.postForEntity("https://codetest.3o3.co.kr/v2/scrap", requestEntity, String.class);

            String scrapResult = response.getBody();

            ScrapData scrapData = new ScrapData();
            scrapData.setUserId(currentUser.getUserId());
            scrapData.setScrapResult(scrapResult);
            scrapDataRepository.save(scrapData);

            return ResponseEntity.ok(scrapResult);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }
    }

    @GetMapping("/refund")
    public ResponseEntity<?> calculateRefund(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 토큰이 필요합니다.");
        }

        try {
            String userId = jwtTokenService.extractUserId(token.substring(7));
            Optional<User> user = userService.findByUserId(userId);
            if (!user.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
            }

            Optional<ScrapData> scrapDataOpt = scrapDataRepository.findByUserId(userId);
            if (!scrapDataOpt.isPresent()) {
                return ResponseEntity.badRequest().body("스크랩 데이터가 존재하지 않습니다.");
            }

            ScrapData scrapData = scrapDataOpt.get();
            JSONObject taxResult = TaxCalculator.calculateTax(scrapData.getScrapResult());

            JSONObject response = new JSONObject();
            response.put("이름", user.get().getName());
            response.put("결정세액", taxResult.get("결정세액"));
            response.put("퇴직연금세액공제", taxResult.get("퇴직연금세액공제"));

            return ResponseEntity.ok().body(response.toString());

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 유효하지 않습니다.");
        } catch (Exception e) {
            log.error("Internal server error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("내부 서버 오류");
        }
    }
}
