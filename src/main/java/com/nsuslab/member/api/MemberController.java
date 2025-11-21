package com.nsuslab.member.api;

import com.nsuslab.member.api.dto.request.MemberCreateRequest;
import com.nsuslab.member.api.dto.response.MemberResponse;
import com.nsuslab.member.service.AuthService;
import com.nsuslab.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;

    @PostMapping("/members")
    public void signup(@Valid @RequestBody MemberCreateRequest memberCreateRequest) {
        memberService.createMember(MemberCreateRequest.toEntity(memberCreateRequest));
    }

    @GetMapping("/members/{loginId}")
    public MemberResponse getMember(@PathVariable String loginId) {
        return memberService.getMember(loginId);
    }

    @PostMapping("/reissue")
    public void reissueToken(HttpServletRequest request, HttpServletResponse response) {
        authService.reissue(request, response);
    }

//    @PostMapping("/login")
//    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
//        return authService.login(request.getLoginId(), request.getPassword());
//    }

//    @PostMapping("/logout")
//    public void logout(HttpServletRequest request, HttpServletResponse response) {
////        authService.logout(request, response);
//    }

//    @PostMapping("/refresh")
//    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
//        memberService.refreshToken(request, response);
//    }

}
