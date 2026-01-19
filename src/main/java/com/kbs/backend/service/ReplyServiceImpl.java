package com.kbs.backend.service;

import com.kbs.backend.domain.Board;
import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Reply;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.dto.ReplyDTO;
import com.kbs.backend.repository.BoardRepository;
import com.kbs.backend.repository.MemberRepository;
import com.kbs.backend.repository.ReplyRepository;
import com.kbs.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReplyServiceImpl implements ReplyService {
    @Autowired
    private ReplyRepository replyRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BoardRepository boardRepository;

    @Override
    public Long register(ReplyDTO replyDTO) {
        Reply reply = dtoTOEntity(replyDTO);
        Board board = boardRepository.findById(reply.getBoard().getId()).orElse(null);
        Member member = memberRepository.findById(reply.getMember().getId()).orElse(null);
        reply.setBoard(board);
        reply.setMember(member);
        Long id =  replyRepository.save(reply).getId();
        return id;
    }

    @Override
    public PageResponseDTO<ReplyDTO> getList(PageRequestDTO pageRequestDTO, Long bno) {
        Pageable pageable = pageRequestDTO.getPageable("id");
        Page<Reply> result = replyRepository.listOfBoard(bno, pageable);
        List<ReplyDTO> dtoList =result.getContent().stream()
                .map(reply -> entityTODto(reply))
                .collect(Collectors.toList());
        return PageResponseDTO.<ReplyDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }

    @Override
    public void modify(ReplyDTO replyDTO) {
        Reply reply = replyRepository.findById(replyDTO.getId()).orElse(null);
        reply.setContent(replyDTO.getContent());
        replyRepository.save(reply);
    }

    @Override
    public void remove(Long id) {
        Reply reply = replyRepository.findById(id).orElse(null);
        reply.setDeleted(true);
        reply.setContent("삭제 된 댓글입니다.");
        replyRepository.save(reply);
    }
}
