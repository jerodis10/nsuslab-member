package com.nsuslab.member.service;

import com.nsuslab.member.api.dto.response.MemberResponse;
import com.nsuslab.member.entity.Member;
import com.nsuslab.member.repository.MemberQueryRepository;
import com.nsuslab.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final PasswordEncoder passwordEncoder;

    public void createMember(Member member) {
        member.passwordEncode(passwordEncoder.encode(member.getPassword()));
        memberRepository.save(member);
    }

    public MemberResponse getMember(String loginId) {
        Member member = memberQueryRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        return MemberResponse.from(member);
    }
}
