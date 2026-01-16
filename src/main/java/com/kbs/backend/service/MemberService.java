package com.kbs.backend.service;

import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Role;
import com.kbs.backend.dto.MemberDTO;


public interface MemberService {
  MemberDTO registerMember(MemberDTO memberDTO);
  MemberDTO findMemberByUsername(String username);
  void changeRole(Role newRole, String username);
  void deleteMember(Long id);
  void changePassword(String username, String newPassword);
  boolean checkPassword(String username, String rawPassword);
  default Member dtoToEntity(MemberDTO memberDTO) {
      return Member.builder()
              .username(memberDTO.getUsername())
              .password(memberDTO.getPassword())
              .name(memberDTO.getName())
              .role(memberDTO.getRole())
              .build();
  }
  default MemberDTO entityToDto(Member member) {
      if(member == null) return null;
      return MemberDTO.builder()
              .id(member.getId())
              .username(member.getUsername())
              .name(member.getName())
              .role(member.getRole())
              .build();
  }
}
