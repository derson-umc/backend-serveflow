package com.serveflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.repository.user.UserRepository;

@Slf4j
@SpringBootApplication
public class ServeFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServeFlowApplication.class, args);
    }

    private static final String ROOT_JOB_POSITION = "ADMIN";

    @Bean
    CommandLineRunner init(
            UserRepository repo,
            PasswordEncoder encoder,
            @Value("${ROOT_USERNAME}") String rootUsername,
            @Value("${ROOT_PASSWORD}") String rootPassword) {
        return args -> {
            String normalizedRoot = rootUsername.trim().toLowerCase(java.util.Locale.ROOT);
            var existing = repo.findByUsername(normalizedRoot);
            if (existing.isEmpty()) {
                repo.save(User.create(
                        normalizedRoot,
                        encoder.encode(rootPassword),
                        UserRole.ADMIN,
                        ROOT_JOB_POSITION
                ));
                log.info("Usuário ADMIN criado com sucesso");
            } else if (!encoder.matches(rootPassword, existing.get().getPassword())) {
                User current = existing.get();
                User updated = new User(
                        current.getId(),
                        normalizedRoot,
                        encoder.encode(rootPassword),
                        UserRole.ADMIN,
                        current.getJobposition() != null ? current.getJobposition() : ROOT_JOB_POSITION
                );
                repo.save(updated);
                log.info("Senha do usuário ADMIN atualizada.");
            }
        };
    }
}
