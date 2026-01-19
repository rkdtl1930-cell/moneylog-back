package com.kbs.backend.service;

import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Role;
import com.kbs.backend.dto.MemberDTO;
import com.kbs.backend.repository.MemberRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MemberServiceImpl implements MemberService {
    @Autowired
    private MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public MemberServiceImpl(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public MemberDTO registerMember(MemberDTO memberDTO) {
        if(memberRepository.existsByUsername(memberDTO.getUsername())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        Member member = dtoToEntity(memberDTO);
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member.setRole(Role.USER);
        Member saved = memberRepository.save(member);
        return entityToDto(saved);
    }

    @Override
    public MemberDTO findMemberByUsername(String username) {
        Member member = memberRepository.findByUsername(username);
        if(member == null) {
            return null;
        }
        return entityToDto(member);
    }

    @Override
    public void changeRole(Role newRole, String username) {
        memberRepository.updateMemberRole(username, newRole);
    }

    @Override
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    @Override
    public void changePassword(String username, String newPassword) {
        Member member = memberRepository.findByUsername(username);
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    @Override
    public boolean checkPassword(String username, String rawPassword) {
        Member member = memberRepository.findByUsername(username);
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

    @Override
    public void changeInteresting(String username, String interesting) {
        Member member = memberRepository.findByUsername(username);
        member.setInteresting(interesting);
        memberRepository.save(member);
    }
}
