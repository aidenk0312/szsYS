package szs.YS.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import szs.YS.user.entity.User;
import szs.YS.user.service.JwtTokenService;
import szs.YS.user.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService, jwtTokenService);
    }

    @Test
    @DisplayName("정보 조회 성공 테스트")
    void getMyInfo_Success() {
        // Given
        String userId = "testUser";
        String token = "Bearer validToken";
        User mockUser = new User();
        mockUser.setUserId(userId);

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtTokenService.extractUserId(token.substring(7))).thenReturn(userId);
        when(userService.findByUserId(userId)).thenReturn(Optional.of(mockUser));

        // When
        ResponseEntity<?> response = userController.getMyInfo(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockUser, response.getBody());
    }
}