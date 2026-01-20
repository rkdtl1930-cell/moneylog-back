package com.kbs.backend.service;

import com.kbs.backend.domain.Board;
import com.kbs.backend.domain.Member;
import com.kbs.backend.dto.BoardDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.repository.BoardRepository;
import com.kbs.backend.repository.MemberRepository;
import com.kbs.backend.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardServiceImpl implements  BoardService {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public Long registerBoard(BoardDTO boardDTO) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long mid = userPrincipal.getId();
        Member member = memberRepository.findById(mid).orElse(null);
        Board board = Board.builder()
                .title(boardDTO.getTitle())
                .content(boardDTO.getContent())
                .imageUrl(boardDTO.getImageUrl())
                .member(member)
                .build();
        Board saved = boardRepository.save(board);
        return saved.getId();
    }

    @Override
    public BoardDTO findBoard(Long id) {
        Board board = boardRepository.findById(id).orElse(null);
        board.updateReadCount();
        boardRepository.save(board);
        return entityToDto(board);
    }

    @Override
    public void updateBoard(BoardDTO boardDTO) {
        Board board = boardRepository.findById(boardDTO.getId()).orElse(null);
        if(board!=null){
            board.setTitle(boardDTO.getTitle());
            board.setContent(boardDTO.getContent());
            boardRepository.save(board);
        }
    }

    @Override
    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }

    @Override
    public PageResponseDTO<BoardDTO> getBoards(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("id");
        Page<BoardDTO> result = boardRepository.searchAll(pageRequestDTO.getTypes(), pageRequestDTO.getKeyword(), pageable);
        return PageResponseDTO.<BoardDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(result.getContent())
                .total((int)result.getTotalElements())
                .build();
    }
}
