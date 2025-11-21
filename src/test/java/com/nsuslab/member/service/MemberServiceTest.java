package com.nsuslab.member.service;

import com.nsuslab.member.api.dto.response.MemberResponse;
import com.nsuslab.member.repository.MemberQueryRepository;
import com.nsuslab.member.repository.MemberRepository;
import com.nsuslab.member.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberQueryRepository memberQueryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Member member;

    @BeforeEach
    void setUp() {
//        MockitoAnnotations.openMocks(this);
        member = new Member("testuser", "password123", "USER");
    }

    @Test
    @DisplayName("회원 생성 성공")
    void createMember_success() {
        // given
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willReturn(member);

        // when
        memberService.createMember(member);

        // then
        then(memberRepository).should().save(member);
        assertThat(member.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("회원 조회 성공")
    void getMember_success() {
        // given
        given(memberQueryRepository.findByLoginId("testuser")).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.getMember("testuser");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getLoginId()).isEqualTo("testuser");
        then(memberQueryRepository).should().findByLoginId("testuser");
    }

    @Test
    @DisplayName("회원 조회 실패 - 회원 없음")
    void getMember_notFound() {
        // given
        given(memberQueryRepository.findByLoginId("unknown")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMember("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("회원 정보를 찾을 수 없습니다.");
        then(memberQueryRepository).should().findByLoginId("unknown");
    }
}