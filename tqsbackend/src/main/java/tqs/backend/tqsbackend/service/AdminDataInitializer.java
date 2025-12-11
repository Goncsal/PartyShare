package tqs.backend.tqsbackend.service;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.UserRepository;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminDataInitializer.class);
    private final UserRepository userRepository;

    public AdminDataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @org.springframework.beans.factory.annotation.Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (adminPassword == null || adminPassword.isBlank()) {
            logger.warn("Admin password not set. Skipping admin seeding.");
            return;
        }

        if (userRepository.findByEmail("admin@partyshare.com").isEmpty()) {
            User admin = new User(
                    "Admin User",
                    "admin@partyshare.com",
                    BCrypt.hashpw(adminPassword, BCrypt.gensalt()),
                    UserRoles.ADMIN);
            userRepository.save(admin);
            logger.info("Admin user seeded: admin@partyshare.com");
        }
    }
}
