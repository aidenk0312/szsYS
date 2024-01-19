package szs.YS.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import szs.YS.user.entity.User;
import szs.YS.user.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.encoder = new BCryptPasswordEncoder();
    }

    public User registerUser(String userId, String password, String name, String regNo) {
        // 사용자 목록
        Map<String, String> allowedUsers = new HashMap<>();
        allowedUsers.put("홍길동", "860824-1655068");
        allowedUsers.put("김돌리", "921108-1582816");
        allowedUsers.put("마징가", "880601-2455116");
        allowedUsers.put("베지터", "910411-1656116");
        allowedUsers.put("손오공", "820326-2715702");

        if (!allowedUsers.containsKey(name) || !allowedUsers.get(name).equals(regNo)) {
            throw new IllegalArgumentException("등록이 허용되지 않은 사용자입니다.");
        }

        String encodedPassword = encoder.encode(password);
        String encodedRegNo = encoder.encode(regNo);

        User user = new User(userId, encodedPassword, name, encodedRegNo);
        return userRepository.save(user);
    }

    public Optional<User> authenticateUser(String userId, String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return userRepository.findByUserId(userId)
                .filter(user -> encoder.matches(rawPassword, user.getPassword()));
    }

    public Optional<User> findUserById(String userId) {
        return userRepository.findByUserId(userId);
    }
}