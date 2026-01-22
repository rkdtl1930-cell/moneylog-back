package com.kbs.backend.service;

import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Role;
import com.kbs.backend.dto.MemberDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;


public interface MemberService {
  MemberDTO registerMember(MemberDTO memberDTO);
  MemberDTO findMemberByUsername(String username);
  PageResponseDTO<MemberDTO> getMembers(PageRequestDTO pageRequestDTO);
  void changeRole(Role newRole, String username);
  void deleteMember(Long id);
  void changePassword(String username, String newPassword);
  boolean checkPassword(String username, String rawPassword);
  void changeInfo(String username, String newPassword, String nickname);

  default Member dtoToEntity(MemberDTO memberDTO) {
      return Member.builder()
              .username(memberDTO.getUsername())
              .password(memberDTO.getPassword())
              .name(memberDTO.getName())
              .role(memberDTO.getRole())
              .nickname(memberDTO.getNickname())
              .build();
  }
  default MemberDTO entityToDto(Member member) {
      if(member == null) return null;
      return MemberDTO.builder()
              .id(member.getId())
              .username(member.getUsername())
              .name(member.getName())
              .role(member.getRole())
              .nickname(member.getNickname())
              .build();
  }
}
