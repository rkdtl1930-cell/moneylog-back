package com.kbs.backend.service;

import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Role;
import com.kbs.backend.dto.MemberDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.repository.MemberRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public PageResponseDTO<MemberDTO> getMembers(PageRequestDTO pageRequestDTO) {
        Pageable pageable =pageRequestDTO.getPageable("id");
        Page<Member> result = memberRepository.findAll(pageable);
        List<MemberDTO> dtoList = result.getContent().stream().map(this::entityToDto).toList();
        return PageResponseDTO.<MemberDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
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

    @Override
    public void changeInfo(String username, String newPassword, String interesting, String nickname) {
        Member member = memberRepository.findByUsername(username);
        if(interesting != null && !interesting.isBlank()) {
            member.setInteresting(interesting);
        }
        if(nickname != null && !nickname.isBlank()) {
            member.setNickname(nickname);
        }
        if(newPassword != null && !newPassword.isBlank()) {
            member.setPassword(passwordEncoder.encode(newPassword));
        }
        memberRepository.save(member);
    }
}
