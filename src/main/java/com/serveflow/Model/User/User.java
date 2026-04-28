package com.serveflow.Model.User;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class User implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final UserRole role;
    private final String jobposition;

    public User(Long id, String username, String password, UserRole role, String jobposition) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "Username é obrigatório.");
        this.password = Objects.requireNonNull(password, "Senha é obrigatória.");
        this.role = Objects.requireNonNull(role, "Role é obrigatória.");
        this.jobposition = Objects.requireNonNull(jobposition, "É obrigatório informar o cargo.");
    }

    public static User create(String username, String encodedPassword, UserRole role,  String jobposition) {
        return new User(null, username, encodedPassword, role, jobposition);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority("ROLE_" + p))
                .collect(Collectors.toList());
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
