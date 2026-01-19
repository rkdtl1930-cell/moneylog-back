package com.kbs.backend.controller;

import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.dto.ReplyDTO;
import com.kbs.backend.service.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/replies")
public class ReplyController {
    @Autowired
    private ReplyService replyService;

    @PostMapping
    public ResponseEntity<Long> register(@RequestBody ReplyDTO replyDTO){
        Long id = replyService.register(replyDTO);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/board/{bno}")
    public ResponseEntity<PageResponseDTO<ReplyDTO>> getList(@PathVariable Long bno, PageRequestDTO pageRequestDTO){
        PageResponseDTO<ReplyDTO> response = replyService.getList(pageRequestDTO, bno);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> modify(@PathVariable Long id, @RequestBody ReplyDTO replyDTO){
        replyDTO.setId(id);
        replyService.modify(replyDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id){
        replyService.remove(id);
        return ResponseEntity.ok().build();
    }
}
