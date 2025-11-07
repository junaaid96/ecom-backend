package com.ecom.seeder;

import com.ecom.entity.User;
import com.ecom.entity.enums.UserRole;
import com.ecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
    }

    private void seedAdmin() {
        userRepository.findByEmail("admin@shop.com").ifPresentOrElse(
                user -> {},
                () -> userRepository.save(User.builder()
                        .username("admin")
                        .email("admin@shop.com")
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(UserRole.ROLE_ADMIN)
                        .build())
        );
    }
}

