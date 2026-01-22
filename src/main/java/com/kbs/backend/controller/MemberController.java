package com.kbs.backend.controller;

import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Role;
import com.kbs.backend.dto.MemberDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.security.UserPrincipal;
import com.kbs.backend.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @GetMapping("/list")
    public PageResponseDTO<MemberDTO> getMembers(PageRequestDTO pageRequestDTO) {
      return memberService.getMembers(pageRequestDTO);
    }

    @PutMapping("/change/{username}/{role}")
    ResponseEntity<Object> changeRole(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Role role, @PathVariable String username) {
        memberService.changeRole(role,username);
        return ResponseEntity.ok(true);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok().build();
    }

//    @PutMapping("/change-password")
//    public ResponseEntity<String> changePassword(@AuthenticationPrincipal UserPrincipal principal, @RequestBody Map<String, String> request) {
//        String newPassword = request.get("password");
//        if(newPassword == null || newPassword.isBlank()) {
//            return ResponseEntity.badRequest().body("Password is empty");
//        }
//        memberService.changePassword(principal.getUsername(), newPassword);
//        return ResponseEntity.ok("Password changed successfully");
//    }

    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@AuthenticationPrincipal UserPrincipal principal, @RequestBody Map<String, String> request) {
        String password = request.get("password");
        boolean matches = memberService.checkPassword(principal.getUsername(),password);
        return ResponseEntity.ok(matches);
    }

//    // 회원 수정에서 관심분야 변경
//    @PostMapping("/change-interesting")
//    public ResponseEntity<String> changeInteresting(@AuthenticationPrincipal UserPrincipal principal, @RequestBody Map<String, String> request) {
//        String interesting = request.get("interesting");
//        memberService.changeInteresting(principal.getUsername(), interesting);
//        return ResponseEntity.ok("Interesting changed successfully");
//    }

    // 회원 정보 수정 - 비밀번호, 관심사, 닉네임
    @PutMapping("/change-info")
    public ResponseEntity<String> changeInfo(@AuthenticationPrincipal UserPrincipal principal, @RequestBody Map<String, String> request) {
        String newPassword = request.get("password");
        String interesting = request.get("interesting");
        String nickname = request.get("nickname");
        if((newPassword == null || newPassword.isBlank())&&
            (interesting == null || interesting.isBlank())&&
            (nickname == null || nickname.isBlank())) {
            return ResponseEntity.badRequest().body("변경할 값이 없습니다.");
        }
        if (request.containsKey("password") &&
                (newPassword == null || newPassword.isBlank())) {
            return ResponseEntity.badRequest().body("비밀번호는 비어 있을 수 없습니다.");
        }
        if (request.containsKey("nickname") &&
                (nickname == null || nickname.isBlank())) {
            return ResponseEntity.badRequest().body("닉네임은 비어 있을 수 없습니다.");
        }
        if (request.containsKey("interesting") &&
                (interesting == null || interesting.isBlank())) {
            return ResponseEntity.badRequest().body("관심 사항은 비어 있을 수 없습니다.");
        }
        memberService.changeInfo(principal.getUsername(), newPassword, interesting, nickname);
        return ResponseEntity.ok("회원 정보가 수정되었습니다.");
    }
}
