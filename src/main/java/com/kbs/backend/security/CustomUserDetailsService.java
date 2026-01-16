package com.kbs.backend.security;

import com.kbs.backend.domain.Member;
import com.kbs.backend.repository.MemberRepository;
import com.kbs.backend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username);
        if(member == null) {
            throw new UsernameNotFoundException(username);
        }
        Set<GrantedAuthority> authorities = Set.of(SecurityUtils
                .convertToAuthority(member.getRole().name()));
        return UserPrincipal.builder()
                .username(member.getUsername())
                .password(member.getPassword())
                .authorities(authorities)
                .member(member)
                .id(member.getId())
                .build();
    }
}
