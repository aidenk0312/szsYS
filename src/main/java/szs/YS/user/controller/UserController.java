package szs.YS.user.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import szs.YS.user.entity.User;
import szs.YS.user.model.LoginDTO;
import szs.YS.user.model.UserDTO;
import szs.YS.user.service.UserService;
import szs.YS.user.service.JwtTokenService;

import java.util.Optional;

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
}