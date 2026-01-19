package com.kbs.backend.service;

import com.kbs.backend.domain.Reply;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.dto.ReplyDTO;

import java.util.List;

public interface ReplyService {
    Long register(ReplyDTO replyDTO);
    PageResponseDTO<ReplyDTO> getList(PageRequestDTO pageRequestDTO, Long bno);
    void modify(ReplyDTO replyDTO);
    void remove(Long id);

    default Reply dtoTOEntity(ReplyDTO replyDTO) {
        Reply reply = Reply.builder()
                .id(replyDTO.getId())
                .content(replyDTO.getContent())
                .deleted(replyDTO.isDeleted())
                .build();
        return reply;
    }

    default ReplyDTO entityTODto(Reply reply) {
        ReplyDTO replyDTO = ReplyDTO.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .deleted(reply.isDeleted())
                .bno(reply.getBoard().getId())
                .mid(reply.getMember().getId())
                .writer(reply.getMember().getUsername())
                .build();
        return replyDTO;
    }
}
