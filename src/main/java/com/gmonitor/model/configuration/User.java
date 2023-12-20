package com.gmonitor.model.configuration;

import lombok.Builder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Setter
@Builder
public class User implements UserDetails {

    private Collection<? extends GrantedAuthority> authorities;
    private String password;
    private String username;
    private String firstName;
    private String lastName;
    private boolean active;
    private List<UserRole> roles;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.info("Roles: " + roles);
        return roles.stream().map(UserRole::getValue).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public List<UserRole> getRoles() { return roles; }

    public String getPassword() { return password; }

    public String getUsername() { return username; }

    public boolean isAccountNonExpired() { return  active; }

    public boolean isAccountNonLocked() { return active; }

    public boolean isCredentialsNonExpired() { return active; }

    public boolean isEnabled() { return active; }

}
