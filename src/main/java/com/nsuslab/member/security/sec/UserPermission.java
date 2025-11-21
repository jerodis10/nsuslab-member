package com.nsuslab.member.security.sec;

public enum UserPermission {

    USER("user"),
    MEMBER_READ("member:read"),
    MEMBER_WRITE("member:write");

    private final String permission;

    UserPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
