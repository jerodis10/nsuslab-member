package com.nsuslab.member.security.sec;


import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nsuslab.member.security.sec.UserPermission.*;


@RequiredArgsConstructor
public enum UserRole {

    ROLE_USER(Sets.newHashSet(USER)),
    ROLE_ADMINTRAINEE(Sets.newHashSet(MEMBER_READ, USER)),
    ROLE_ADMIN(Sets.newHashSet(MEMBER_READ, MEMBER_WRITE, USER));

    private final Set<UserPermission> permissions;

    public Set<UserPermission> getPermissions() {
        return permissions;
    }

    public Set<SimpleGrantedAuthority> grantedAuthorities() {
        Set<SimpleGrantedAuthority> permissions = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());

        permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return permissions;
    }

    public static Optional<UserRole> from(String role) {
        return Arrays.stream(values())
                .filter(r -> r.name().equals(role))
                .findFirst();
    }
}
