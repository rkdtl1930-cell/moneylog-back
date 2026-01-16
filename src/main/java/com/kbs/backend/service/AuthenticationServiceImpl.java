package com.kbs.backend.service;

import com.kbs.backend.domain.Member;
import com.kbs.backend.dto.MemberDTO;
import com.kbs.backend.security.UserPrincipal;
import com.kbs.backend.security.jwt.JwtProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtProvider jwtProvider;

    @Override
    public MemberDTO signInAndReturnJWT(MemberDTO signInRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword())
        );
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Member member = userPrincipal.getMember();
        String jwt = jwtProvider.generateToken(userPrincipal);
        MemberDTO memberDTO = MemberDTO.builder()
                .id(member.getId())
                .username(member.getUsername())
                .password(member.getPassword())
                .role(member.getRole())
                .name(member.getName())
                .token(jwt)
                .build();
        return memberDTO;
    }
}
