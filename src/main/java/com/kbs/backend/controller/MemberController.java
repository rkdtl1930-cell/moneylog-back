package com.kbs.backend.controller;

import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Role;
import com.kbs.backend.dto.MemberDTO;
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

    @GetMapping("/{username}")
    public ResponseEntity<MemberDTO> getMember(@PathVariable String username) {
        MemberDTO memberDTO = memberService.findMemberByUsername(username);
        if(memberDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(memberDTO);
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

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal UserPrincipal principal, @RequestBody Map<String, String> request) {
        String newPassword = request.get("password");
        if(newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Password is empty");
        }
        memberService.changePassword(principal.getUsername(), newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@AuthenticationPrincipal UserPrincipal principal, @RequestBody Map<String, String> request) {
        String password = request.get("password");
        boolean matches = memberService.checkPassword(principal.getUsername(),password);
        return ResponseEntity.ok(matches);
    }

    // 회원 수정에서 관심분야 변경
    @PostMapping("/change-interesting")
    public ResponseEntity<String> changeInteresting(@AuthenticationPrincipal UserPrincipal principal, @RequestBody Map<String, String> request) {
        String interesting = request.get("interesting");
        memberService.changeInteresting(principal.getUsername(), interesting);
        return ResponseEntity.ok("Interesting changed successfully");
    }
}
