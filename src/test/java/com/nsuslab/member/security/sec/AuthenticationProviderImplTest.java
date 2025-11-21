package com.nsuslab.member.security.sec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthenticationProviderImplTest {

    private UserDetailsServiceImpl userDetailsService;
    private PasswordEncoder passwordEncoder;
    private AuthenticationProviderImpl authenticationProvider;

    @BeforeEach
    void setUp() {
        userDetailsService = mock(UserDetailsServiceImpl.class);
        passwordEncoder = mock(PasswordEncoder.class);

        authenticationProvider = new AuthenticationProviderImpl(
                userDetailsService, passwordEncoder
        );
    }

    @Test
    @DisplayName("정상 인증 성공")
    void authenticate_success() {
        // given
        String username = "testuser";
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$abcd1234encoded";
        Set<SimpleGrantedAuthority> authorities =
                Set.of(new SimpleGrantedAuthority("ROLE_USER"));

        UserDetailsImpl userDetails = new UserDetailsImpl(username, encodedPassword, authorities, true, true, true, true);

        UsernamePasswordAuthenticationToken inputToken =
                new UsernamePasswordAuthenticationToken(username, rawPassword);

        // stubbing
        given(userDetailsService.loadUserByUsername(username))
                .willReturn(userDetails);
        given(passwordEncoder.matches(rawPassword, encodedPassword))
                .willReturn(true);

        // when
        UsernamePasswordAuthenticationToken result =
                (UsernamePasswordAuthenticationToken) authenticationProvider.authenticate(inputToken);

        // then
        assertThat(result.getPrincipal()).isEqualTo(username);
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        then(userDetailsService).should().loadUserByUsername(username);
        then(passwordEncoder).should().matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 → UsernameNotFoundException 발생")
    void authenticate_userNotFound() {
        // given
        String username = "unknown";
        String password = "pass";

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, password);

        given(userDetailsService.loadUserByUsername(username))
                .willThrow(new UsernameNotFoundException("User not found"));

        // when & then
        assertThatThrownBy(() -> authenticationProvider.authenticate(token))
                .isInstanceOf(UsernameNotFoundException.class);

        then(userDetailsService).should().loadUserByUsername(username);
        then(passwordEncoder).should(never()).matches(any(), any());
    }

    @Test
    @DisplayName("비밀번호 불일치 → BadCredentialsException 발생")
    void authenticate_invalidPassword() {
        // given
        String username = "testuser";
        String rawPassword = "wrongPassword";
        String encodedPassword = "$2a$10$abcd1234encoded";
        Set<SimpleGrantedAuthority> authorities =
                Set.of(new SimpleGrantedAuthority("ROLE_USER"));

        UserDetailsImpl userDetails = new UserDetailsImpl(username, encodedPassword, authorities, true, true, true, true);

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, rawPassword);

        given(userDetailsService.loadUserByUsername(username))
                .willReturn(userDetails);
        given(passwordEncoder.matches(rawPassword, encodedPassword))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> authenticationProvider.authenticate(token))
                .isInstanceOf(BadCredentialsException.class);

        then(userDetailsService).should().loadUserByUsername(username);
        then(passwordEncoder).should().matches(rawPassword, encodedPassword);
    }
}