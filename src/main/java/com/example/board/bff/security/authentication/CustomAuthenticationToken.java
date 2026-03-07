package com.example.board.bff.security.authentication;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {
    @Getter
    private TokenPair tokenPair;

    private CustomAuthenticationToken(@Nullable Object principal, @Nullable Object credentials) {
        super(principal, credentials);
    }

    private CustomAuthenticationToken(Object principal, @Nullable Object credentials, Collection<? extends GrantedAuthority> authorities, TokenPair tokenPair) {
        super(principal, credentials, authorities);
        this.tokenPair = tokenPair;
    }

    public static CustomAuthenticationToken unauthenticated(@Nullable Object principal, @Nullable Object credentials) {
        return new CustomAuthenticationToken(principal, credentials);
    }

    public static CustomAuthenticationToken authenticated(Object principal, @Nullable Object credentials, Collection<? extends GrantedAuthority> authorities, TokenPair tokenPair) {
        return new CustomAuthenticationToken(principal, credentials, authorities, tokenPair);
    }
}
