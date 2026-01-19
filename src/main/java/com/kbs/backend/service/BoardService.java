// 검색기능 넣어야함 페이징 처리는 완료됨
package com.kbs.backend.service;

import com.kbs.backend.domain.Board;
import com.kbs.backend.domain.Notice;
import com.kbs.backend.dto.BoardDTO;
import com.kbs.backend.dto.NoticeDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;

public interface BoardService {
    Long registerBoard(BoardDTO boardDTO);
    BoardDTO findBoard(Long id);
    void updateBoard(BoardDTO boardDTO);
    void deleteBoard(Long id);
    PageResponseDTO<BoardDTO> getBoards(PageRequestDTO pageRequestDTO);

    default Board dtoToEntity(BoardDTO boardDTO) {
        Board board = Board.builder()
                .id(boardDTO.getId())
                .title(boardDTO.getTitle())
                .content(boardDTO.getContent())
                .build();
        return board;
    }

    default BoardDTO entityToDto(Board board) {
        BoardDTO boardDTO = BoardDTO.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .createTime(board.getCreateTime())
                .updateTime(board.getUpdateTime())
                .mid(board.getMember().getId())
                .writer(board.getMember().getUsername())
                .build();
        return boardDTO;
    }
}
