package com.kbs.backend.controller;

import com.kbs.backend.domain.Member;
import com.kbs.backend.dto.MemberDTO;
import com.kbs.backend.service.AuthenticationService;
import com.kbs.backend.service.MemberService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/authentication")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private MemberService memberService;

    @PostMapping("/sign-up")
    public ResponseEntity<MemberDTO> signup(@RequestBody MemberDTO memberDTO){
        if(memberService.findMemberByUsername(memberDTO.getUsername())!=null){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        MemberDTO saved = memberService.registerMember(memberDTO);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<Object> signin(@RequestBody MemberDTO memberDTO){
        return new ResponseEntity<>(authenticationService.signInAndReturnJWT(memberDTO), HttpStatus.OK);
    }

    @GetMapping("/check-username")
    public ResponseEntity<Object> checkUsername(@RequestParam String username){
        boolean exists = memberService.findMemberByUsername(username) != null;
        return ResponseEntity.ok(Map.of("available", !exists));
    }
}
