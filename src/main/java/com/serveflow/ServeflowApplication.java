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
public class ServeflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServeflowApplication.class, args);
    }

    private static final String ROOT_JOB_POSITION = "ROOT";

    @Bean
    CommandLineRunner init(
            UserRepository repo,
            PasswordEncoder encoder,
            @Value("${ROOT_USERNAME:root}") String rootUsername,
            @Value("${ROOT_PASSWORD}") String rootPassword) {
        return args -> {
            var existing = repo.findByUsername(rootUsername);
            if (existing.isEmpty()) {
                repo.save(User.create(
                        rootUsername,
                        encoder.encode(rootPassword),
                        UserRole.ROOT,
                        ROOT_JOB_POSITION
                ));
                log.info("Usuário ROOT criado com sucesso");
            } else if (!encoder.matches(rootPassword, existing.get().getPassword())) {
                User current = existing.get();
                User updated = new User(
                        current.getId(),
                        rootUsername,
                        encoder.encode(rootPassword),
                        UserRole.ROOT,
                        current.getJobposition() != null ? current.getJobposition() : ROOT_JOB_POSITION
                );
                repo.save(updated);
                log.info("Senha do usuário ROOT atualizada");
            }
        };
    }
}
