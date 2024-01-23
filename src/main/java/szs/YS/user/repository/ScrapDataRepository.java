package szs.YS.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import szs.YS.user.entity.ScrapData;
import java.util.Optional;

public interface ScrapDataRepository extends JpaRepository<ScrapData, Long> {
    Optional<ScrapData> findByUserId(String userId);
}