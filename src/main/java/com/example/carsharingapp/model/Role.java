package com.example.carsharingapp.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_MANAGER,
    ROLE_CUSTOMER;

    @Override
    public String getAuthority() {
        return name();
    }
}
