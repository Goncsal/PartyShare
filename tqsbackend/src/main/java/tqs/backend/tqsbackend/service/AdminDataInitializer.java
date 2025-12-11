package tqs.backend.tqsbackend.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.UserRepository;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public AdminDataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@partyshare.com").isEmpty()) {
            User admin = new User(
                    "Admin User",
                    "admin@partyshare.com",
                    BCrypt.hashpw("admin123", BCrypt.gensalt()),
                    UserRoles.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin user seeded: admin@partyshare.com / admin123");
        }
    }
}
