package com.nsuslab.member.security.sec;

import com.nsuslab.member.entity.Member;
import com.nsuslab.member.repository.MemberQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberQueryRepository memberQueryRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberQueryRepository.findByLoginId(loginId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Member not found with loginId: " + loginId));

        // 권한 설정
        Set<SimpleGrantedAuthority> authorities = UserRole.from(member.getRole())
                .map(UserRole::grantedAuthorities)
                .orElseThrow(() -> new IllegalStateException("Invalid role: " + member.getRole()));

//        Set<SimpleGrantedAuthority> authority = null;
//        for (UserRole role : UserRole.values()) {
//            if(member.get().getRole().equals(role.name())) authority = role.grantedAuthorities();
//        }

        return new UserDetailsImpl(
                member.getLoginId(),
                member.getPassword(),
                authorities,
                true,
                true,
                true,
                true
        );
    }
}
