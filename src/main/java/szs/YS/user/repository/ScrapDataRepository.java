package szs.YS.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import szs.YS.user.entity.ScrapData;

public interface ScrapDataRepository extends JpaRepository<ScrapData, Long> {
}