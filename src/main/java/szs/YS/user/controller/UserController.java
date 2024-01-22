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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import szs.YS.user.entity.User;
import szs.YS.user.model.LoginDTO;
import szs.YS.user.model.UserDTO;
import szs.YS.user.service.UserService;
import szs.YS.user.service.JwtTokenService;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/szs")
public class UserController {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;

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
    @Operation(parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "Authorization", description = "JWT Token", required = false)
            })
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
}