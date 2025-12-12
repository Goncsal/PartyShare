package tqs.backend.tqsbackend.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsById(Long id);

    boolean existsByEmail(String email);

    Optional<User> findById(Long id);

    List<User> findByNameContainingIgnoreCase(String name);

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRoles role);

    List<User> findByIsActive(boolean isActive);

    List<User> findByNameContainingIgnoreCaseAndIsActive(String name, boolean isActive);

    List<User> findByNameContainingIgnoreCaseAndRole(String name, UserRoles role);

    List<User> findByRoleAndIsActive(UserRoles role, boolean isActive);

    List<User> findByNameContainingIgnoreCaseAndRoleAndIsActive(String name, UserRoles role, boolean isActive);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:startDate IS NULL OR u.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR u.createdAt <= :endDate)")
    List<User> searchUsers(@org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("role") UserRoles role,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);
}