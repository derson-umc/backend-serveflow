package com.serveflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.serveflow.Model.User.User;
import com.serveflow.Model.User.UserRole;
import com.serveflow.Repository.User.UserRepository;

@Slf4j
@SpringBootApplication
public class ServeFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServeFlowApplication.class, args);
    }

    @Bean
    CommandLineRunner init(
            UserRepository repo,
            PasswordEncoder encoder,
            @Value("${ROOT_USERNAME:admin}") String adminUsername,
            @Value("${ROOT_PASSWORD:admin123}") String adminPassword) {
        return args -> {
            var existing = repo.findByUsername(adminUsername);
            if (existing.isEmpty()) {
                repo.save(User.create(
                        adminUsername,
                        encoder.encode(adminPassword),
                        UserRole.ADMIN,
                        "ADMIN"
                ));
                log.info("Usuário ADMIN criado com sucesso");
            } else if (!encoder.matches(adminPassword, existing.get().getPassword())) {
                User current = existing.get();
                User updated = new User(
                        current.getId(),
                        adminUsername,
                        encoder.encode(adminPassword),
                        UserRole.ADMIN,
                        current.getJobposition() != null ? current.getJobposition() : "ADMIN"
                );
                repo.save(updated);
                log.info("Senha do usuário ADMIN atualizada");
            }
        };
    }
}
