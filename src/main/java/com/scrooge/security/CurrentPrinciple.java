package com.scrooge.security;

import com.scrooge.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CurrentPrinciple implements UserDetails {

    private UUID id;
    private String email;
    private String password;
    private Role role;
    private boolean active;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return Set.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public String getPassword() {

        return this.password;
    }

    @Override
    public String getUsername() {

        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {

        return active;
    }

    @Override
    public boolean isAccountNonLocked() {

        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return active;
    }

    @Override
    public boolean isEnabled() {

        return active;
    }
}
