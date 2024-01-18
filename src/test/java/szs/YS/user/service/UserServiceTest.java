package szs.YS.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import szs.YS.user.entity.User;
import szs.YS.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("등록 된 유저 Test")
    void registerUser_ValidUser_ReturnsUser() {
        // Given
        String userId = "홍길동ID";
        String password = "홍길동PW";
        String name = "홍길동";
        String regNo = "860824-1655068";
        User user = new User(userId, password, name, regNo);

        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.registerUser(userId, password, name, regNo);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("미 등록 된 유저 Test")
    void registerUser_InvalidUser_ThrowsException() {
        // Given
        String userId = "김용수ID";
        String password = "김용수PW";
        String name = "김용수";
        String regNo = "123456-7890123";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(userId, password, name, regNo);
        });
    }
}
