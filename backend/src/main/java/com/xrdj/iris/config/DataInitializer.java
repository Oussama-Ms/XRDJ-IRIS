package com.xrdj.iris.config;

import com.xrdj.iris.model.Role;
import com.xrdj.iris.model.User;
import com.xrdj.iris.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            User admin =
                    User.builder()
                            .username("admin")
                            .password(passwordEncoder.encode("admin123"))
                            .role(Role.ROLE_ADMIN)
                            .build();
            userRepository.save(admin);
            log.info("Default Admin user created (admin / admin123)");
        }

        if (!userRepository.existsByUsername("user")) {
            User user =
                    User.builder()
                            .username("user")
                            .password(passwordEncoder.encode("user123"))
                            .role(Role.ROLE_USER)
                            .build();
            userRepository.save(user);
            log.info("Default Standard user created (user / user123)");
        }
    }
}
