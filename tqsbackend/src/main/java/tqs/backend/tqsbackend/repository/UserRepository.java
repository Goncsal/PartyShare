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
}