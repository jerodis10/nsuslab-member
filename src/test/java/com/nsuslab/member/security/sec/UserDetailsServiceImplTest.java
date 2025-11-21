package com.nsuslab.member.security.sec;

import com.nsuslab.member.entity.Member;
import com.nsuslab.member.repository.MemberQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private MemberQueryRepository memberQueryRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("정상적으로 Member를 조회하면 UserDetails를 반환한다")
    void loadUserByUsername_success() {
        // given
        Member member = Member.builder()
                .loginId("user1")
                .password("encodedPw")
                .build();

        given(memberQueryRepository.findByLoginId("user1"))
                .willReturn(Optional.of(member));

        // when
        UserDetailsImpl result = (UserDetailsImpl) userDetailsService.loadUserByUsername("user1");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("user1");
        assertThat(result.getPassword()).isEqualTo("encodedPw");
    }

    @Test
    @DisplayName("존재하지 않는 loginId 요청 시 UsernameNotFoundException 발생")
    void loadUserByUsername_notFound() {
        // given
        given(memberQueryRepository.findByLoginId(anyString()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                        userDetailsService.loadUserByUsername("unknown")
                ).isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    @DisplayName("UserDetails의 enabled/locked 상태 등이 true 인지 검증")
    void loadUserByUsername_statusFlags() {
        // given
        Member member = Member.builder()
                .loginId("user2")
                .password("pw2")
                .build();

        given(memberQueryRepository.findByLoginId("user2"))
                .willReturn(Optional.of(member));

        // when
        UserDetailsImpl details = (UserDetailsImpl) userDetailsService.loadUserByUsername("user2");

        // then
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.isEnabled()).isTrue();
    }
}