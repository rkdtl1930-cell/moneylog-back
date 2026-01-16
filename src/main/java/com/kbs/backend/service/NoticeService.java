// 검색기능 넣어야함 페이징 처리는 완료됨
package com.kbs.backend.service;

import com.kbs.backend.domain.Notice;
import com.kbs.backend.dto.NoticeDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;

import java.util.List;

public interface NoticeService {
    Long registerNotice(NoticeDTO noticeDTO);
    NoticeDTO findNotice(Long id);
    void updateNotice(NoticeDTO noticeDTO);
    void deleteNotice(Long id);
    PageResponseDTO<NoticeDTO> getNotices(PageRequestDTO pageRequestDTO);

    default Notice dtoToEntity(NoticeDTO noticeDTO) {
        Notice notice = Notice.builder()
                .id(noticeDTO.getId())
                .title(noticeDTO.getTitle())
                .content(noticeDTO.getContent())
                .build();
        return notice;
    }

    default NoticeDTO entityToDto(Notice notice) {
        NoticeDTO noticeDTO = NoticeDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createTime(notice.getCreateTime())
                .mid(notice.getMember().getId())
                .username(notice.getMember().getUsername())
                .build();
        return noticeDTO;
    }
}
