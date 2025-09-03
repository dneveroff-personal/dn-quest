package dn.quest.config;

import dn.quest.model.entities.user.User;
import dn.quest.model.entities.enums.UserRole;
import dn.quest.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:admin}")
    private String adminPassword;

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // --- Admin user ---
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setPublicName("Administrator");
                admin.setRoles(Set.of(UserRole.ADMIN));
                userRepository.save(admin);
                System.out.println("Admin user created: " + adminUsername);
            }
        };
    }
}
