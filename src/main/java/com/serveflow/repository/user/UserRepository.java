package com.serveflow.repository.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.serveflow.exception.user.UserNotFoundException;
import com.serveflow.model.user.User;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserRepository {

    private final SpringUserRepository springRepository;

    @Transactional
    public User save(User user) {
        UserEntity entity;
        if (user.getId() == null) {
            entity = toEntity(user);
        } else {
            entity = springRepository.findById(user.getId())
                    .orElseThrow(() -> new UserNotFoundException(user.getId()));
            updateEntity(entity, user);
        }
        return toDomain(springRepository.save(entity));
    }

    public Optional<User> findByUsername(String username) {
        return springRepository.findByUsername(username).map(this::toDomain);
    }

    public Optional<User> findById(Long id) {
        return springRepository.findById(id).map(this::toDomain);
    }

    public boolean existsByUsername(String username) {
        return springRepository.existsByUsername(username);
    }

    public List<User> findAll() {
        return springRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional
    public void deleteById(Long id) {
        springRepository.deleteById(id);
    }

    private User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getEmail(), entity.getPassword(), entity.getRole(), entity.getJobposition());
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        updateEntity(entity, user);
        return entity;
    }

    private void updateEntity(UserEntity entity, User user) {
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setRole(user.getRole());
        entity.setJobposition(user.getJobposition());
    }
}
