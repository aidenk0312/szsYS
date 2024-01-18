package szs.YS.user.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import szs.YS.user.entity.User;
import szs.YS.user.model.UserDTO;
import szs.YS.user.service.UserService;

@RestController
@RequestMapping("/api/szs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}