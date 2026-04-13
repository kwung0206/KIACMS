package com.kiacms.global.security;

import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String password;
    private final String name;
    private final RoleType roleType;
    private final UserStatus status;
    private final boolean withdrawn;
    private final List<GrantedAuthority> authorities;

    private CustomUserPrincipal(
            UUID userId,
            String email,
            String password,
            String name,
            RoleType roleType,
            UserStatus status,
            boolean withdrawn
    ) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.name = name;
        this.roleType = roleType;
        this.status = status;
        this.withdrawn = withdrawn;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleType.name()));
    }

    public static CustomUserPrincipal from(User user) {
        return new CustomUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getName(),
                user.getRoleType(),
                user.getStatus(),
                user.getDeletedAt() != null
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !withdrawn;
    }
}
