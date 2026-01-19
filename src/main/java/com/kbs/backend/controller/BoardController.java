package com.kbs.backend.controller;

import com.kbs.backend.dto.BoardDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.service.BoardService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
    @Autowired
    private BoardService boardService;

    @PostMapping
    public ResponseEntity<Long> createBoard(@RequestBody BoardDTO boardDTO) {
        return ResponseEntity.ok(boardService.registerBoard(boardDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardDTO> getBoard(@PathVariable Long id) {
        BoardDTO boardDTO = boardService.findBoard(id);
        if(boardDTO == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(boardDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateBoard(@PathVariable Long id, @RequestBody BoardDTO boardDTO) {
        boardDTO.setId(id);
        boardService.updateBoard(boardDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public PageResponseDTO<BoardDTO> getBoards(PageRequestDTO pageRequestDTO) {
        return boardService.getBoards(pageRequestDTO);
    }
}
