package com.nsuslab.member.api.dto.response;

import com.nsuslab.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class MemberResponse {
    private final Long id;
    private final String loginId;
    private final String password;

    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .loginId(member.getLoginId())
                .password(member.getPassword())
                .build();
    }
}
