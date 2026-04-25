package com.serveflow.Model.User;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
public class User implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final UserRole role;

    public User(Long id, String username, String password, UserRole role) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "Username é obrigatório");
        this.password = Objects.requireNonNull(password, "Senha é obrigatória");
        this.role = Objects.requireNonNull(role, "Role é obrigatória");
    }

    public static User create(String username, String encodedPassword, UserRole role) {
        return new User(null, username, encodedPassword, role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority("PERM_" + p)));
        return authorities;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
