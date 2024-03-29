package szs.YS.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import szs.YS.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
}