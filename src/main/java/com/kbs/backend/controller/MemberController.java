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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    @Autowired
    private MemberService memberService;

    // ADMIN인지 확인한다.
    private boolean isAdmin(UserPrincipal principal) {
        if (principal == null || principal.getAuthorities() == null) return false;
        for (GrantedAuthority a : principal.getAuthorities()) {
            if ("ROLE_ADMIN".equals(a.getAuthority())) return true;
        }
        return false;
    }

    // ADMIN 전용
    @GetMapping("/list")
    public ResponseEntity<PageResponseDTO<MemberDTO>> getMembers(
            @AuthenticationPrincipal UserPrincipal principal,
            PageRequestDTO pageRequestDTO
    ) {
        if (principal == null || principal.getId() == null) {
            return ResponseEntity.status(401).build();
        }
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(memberService.getMembers(pageRequestDTO));
    }

    // ADMIN 전용
    @PutMapping("/change/{username}/{role}")
    public ResponseEntity<Object> changeRole(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Role role,
            @PathVariable String username
    ) {
        if (principal == null || principal.getId() == null) {
            return ResponseEntity.status(401).build();
        }
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).build();
        }
        memberService.changeRole(role, username);
        return ResponseEntity.ok(true);
    }


    // 본인과 ADMIN 접근 가능
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        if (principal == null || principal.getId() == null) {
            return ResponseEntity.status(401).build();
        }
        if (!principal.getId().equals(id) && !isAdmin(principal)) {
            return ResponseEntity.status(403).build();
        }
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

    // 로그인 사용자만 사용 가능
    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> request
    ) {
        if (principal == null || principal.getUsername() == null) {
            return ResponseEntity.status(401).body(false);
        }
        String password = request.get("password");
        if (password == null) {
            return ResponseEntity.badRequest().body(false);
        }
        boolean matches = memberService.checkPassword(principal.getUsername(), password);
        return ResponseEntity.ok(matches);
    }


    // 회원 정보 수정(로그인 사용자만) - 비밀번호, 닉네임
    @PutMapping("/change-info")
    public ResponseEntity<String> changeInfo(@AuthenticationPrincipal UserPrincipal principal, @RequestBody Map<String, String> request) {
        // 인증확인
        if (principal == null || principal.getUsername() == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String newPassword = request.get("password");
        String nickname = request.get("nickname");

        if((newPassword == null || newPassword.isBlank())&&
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
        memberService.changeInfo(principal.getUsername(), newPassword,nickname);
        return ResponseEntity.ok("회원 정보가 수정되었습니다.");
    }
}
