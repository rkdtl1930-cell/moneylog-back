package com.kbs.backend.controller;

import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.dto.ReplyDTO;
import com.kbs.backend.security.UserPrincipal;
import com.kbs.backend.service.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/replies")
public class ReplyController {
    @Autowired
    private ReplyService replyService;

    // 유저 인증 확인을 위한 수정
    @PostMapping
    public ResponseEntity<Long> register(
            @RequestBody ReplyDTO replyDTO,
            @AuthenticationPrincipal UserPrincipal user
    ){
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build(); 
        }
        Long id = replyService.register(replyDTO);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/board/{bno}")
    public ResponseEntity<PageResponseDTO<ReplyDTO>> getList(@PathVariable Long bno, PageRequestDTO pageRequestDTO){
        PageResponseDTO<ReplyDTO> response = replyService.getList(pageRequestDTO, bno);
        return ResponseEntity.ok(response);
    }

    // 유저 인증 확인을 위한 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> modify(
            @PathVariable Long id,
            @RequestBody ReplyDTO replyDTO,
            @AuthenticationPrincipal UserPrincipal user
    ){
        // 사용자가 있는지 확인함
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        ReplyDTO existing = replyService.get(id);

        // 댓글이 있는지 확인함
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        // 소유권 확인(본인 댓글만 수정)
        if (existing.getMid() == null || !existing.getMid().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        // 실제 수정
        replyDTO.setId(id);
        replyService.modify(replyDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user
    ){
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        ReplyDTO existing = replyService.get(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        // 본인 댓글만 삭제 가능
        if (existing.getMid() == null || !existing.getMid().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        replyService.remove(id);
        return ResponseEntity.ok().build();
    }
}
