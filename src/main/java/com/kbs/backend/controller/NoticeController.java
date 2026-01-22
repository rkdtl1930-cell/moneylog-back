package com.kbs.backend.controller;

import com.kbs.backend.dto.NoticeDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.security.UserPrincipal;
import com.kbs.backend.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    //유저 인증을 위해 수정함
    @PostMapping
    public ResponseEntity<Long> create(
            @RequestBody NoticeDTO noticeDTO,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(noticeService.registerNotice(noticeDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeDTO> getNotice(@PathVariable Long id) {
        NoticeDTO noticeDTO = noticeService.findNotice(id);
        if(noticeDTO == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(noticeDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateNotice(@PathVariable Long id, @RequestBody NoticeDTO noticeDTO) {
        noticeDTO.setId(id);
        noticeService.updateNotice(noticeDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        NoticeDTO existing = noticeService.findNotice(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        // 작성자(소유자)만 삭제 가능 (공지사항 정책이 다르면 여기만 변경하면 됨)
        if (existing.getMid() == null || !existing.getMid().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        noticeService.deleteNotice(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public PageResponseDTO<NoticeDTO> getNotices(PageRequestDTO pageRequestDTO) {
        return noticeService.getNotices(pageRequestDTO);
    }

}
