package tqs.backend.tqsbackend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void whenSearchByKeyword_thenReturnMatchingUsers() {
        User user1 = new User("Alice Smith", "alice@example.com", "pass", UserRoles.RENTER);
        User user2 = new User("Bob Jones", "bob@example.com", "pass", UserRoles.OWNER);
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        List<User> found = userRepository.searchUsers("Alice", null, null, null);
        assertThat(found).extracting(User::getName).containsOnly("Alice Smith");

        found = userRepository.searchUsers("example.com", null, null, null);
        assertThat(found).hasSize(2);

        found = userRepository.searchUsers("NonExistent", null, null, null);
        assertThat(found).isEmpty();
    }

    @Test
    void whenFilterByDate_thenReturnMatchingUsers() {
        User user1 = new User("User1", "user1@example.com", "pass", UserRoles.RENTER);
        entityManager.persist(user1);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();

        // Search with range covering now
        List<User> found = userRepository.searchUsers(null, null, now.minusDays(1), now.plusDays(1));
        assertThat(found).hasSize(1);

        // Search with range in the past
        found = userRepository.searchUsers(null, null, now.minusDays(2), now.minusDays(1));
        assertThat(found).isEmpty();

        // Search with range in the future
        found = userRepository.searchUsers(null, null, now.plusDays(1), now.plusDays(2));
        assertThat(found).isEmpty();
    }

    @Test
    void whenFilterByRole_thenReturnMatchingUsers() {
        User renter = new User("Renter", "renter@example.com", "pass", UserRoles.RENTER);
        User owner = new User("Owner", "owner@example.com", "pass", UserRoles.OWNER);
        entityManager.persist(renter);
        entityManager.persist(owner);
        entityManager.flush();

        List<User> found = userRepository.searchUsers(null, UserRoles.RENTER, null, null);
        assertThat(found).extracting(User::getName).containsOnly("Renter");

        found = userRepository.searchUsers(null, UserRoles.OWNER, null, null);
        assertThat(found).extracting(User::getName).containsOnly("Owner");
    }

    @Test
    void whenSearchAndFilter_thenReturnMatchingUsers() {
        User user1 = new User("Target User", "target@example.com", "pass", UserRoles.RENTER);
        User user2 = new User("Other User", "other@example.com", "pass", UserRoles.RENTER);
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();

        List<User> found = userRepository.searchUsers("Target", UserRoles.RENTER, now.minusDays(1), now.plusDays(1));
        assertThat(found).extracting(User::getName).containsOnly("Target User");
    }
}
