package com.kbs.backend.controller;

import com.kbs.backend.dto.NoticeDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody NoticeDTO noticeDTO) {
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
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public PageResponseDTO<NoticeDTO> getNotices(PageRequestDTO pageRequestDTO) {
        return noticeService.getNotices(pageRequestDTO);
    }

}
