package szs.YS.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import szs.YS.user.entity.User;
import szs.YS.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
        encoder = new BCryptPasswordEncoder();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerUser_Success() {
        // Given
        String userId = "testUser";
        String password = "password123";
        String name = "홍길동";
        String regNo = "860824-1655068";

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User passedUser = invocation.getArgument(0);
            passedUser.setUserId(userId);
            passedUser.setPassword(encoder.encode(password));
            passedUser.setName(name);
            passedUser.setRegNo(regNo);
            return passedUser;
        });

        // When
        User result = userService.registerUser(userId, password, name, regNo);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertTrue(encoder.matches(password, result.getPassword()));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 (허용되지 않은 사용자)")
    void registerUser_Failure() {
        // Given
        String userId = "testUser";
        String password = "password123";
        String name = "김철수";
        String regNo = "123456-7890123";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(userId, password, name, regNo);
        });
    }


    @Test
    @DisplayName("유효한 사용자 로그인 Test")
    void User_Valid() {
        // Given
        String userId = "testUser";
        String rawPassword = "testPassword";
        User user = new User();
        user.setUserId(userId);
        user.setPassword(encoder.encode(rawPassword));

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.authenticateUser(userId, rawPassword);

        // Then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
    }

    @Test
    @DisplayName("잘못된 자격 증명으로 로그인 시도 Test")
    void User_UnValid() {
        // Given
        String userId = "testUser";
        String rawPassword = "testPassword";
        User user = new User();
        user.setUserId(userId);
        user.setPassword(encoder.encode("differentPassword"));

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.authenticateUser(userId, rawPassword);

        // Then
        assertFalse(result.isPresent());
    }
}