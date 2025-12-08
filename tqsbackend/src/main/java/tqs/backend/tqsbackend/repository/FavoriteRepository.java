package tqs.backend.tqsbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.backend.tqsbackend.entity.Favorite;
import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserId(Long userId);

    void deleteByUserIdAndItemId(Long userId, Long itemId);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);
}
