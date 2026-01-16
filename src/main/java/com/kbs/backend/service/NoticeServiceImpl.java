package com.kbs.backend.service;

import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Notice;
import com.kbs.backend.dto.NoticeDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.repository.MemberRepository;
import com.kbs.backend.repository.NoticeRepository;
import com.kbs.backend.security.UserPrincipal;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class NoticeServiceImpl implements NoticeService {
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public Long registerNotice(NoticeDTO noticeDTO) {
        Notice notice = dtoToEntity(noticeDTO);
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long mid = userPrincipal.getId();
        Member member = memberRepository.findById(mid).orElse(null);
        notice.setMember(member);
        Notice saved =  noticeRepository.save(notice);
        return saved.getId();
    }

    @Override
    public NoticeDTO findNotice(Long id) {
        Notice notice = noticeRepository.findById(id).orElse(null);
        return entityToDto(notice);
    }

    @Override
    public void updateNotice(NoticeDTO noticeDTO) {
        Notice notice = noticeRepository.findById(noticeDTO.getId()).orElse(null);
        if(notice!=null){
            notice.setTitle(noticeDTO.getTitle());
            notice.setContent(noticeDTO.getContent());
            noticeRepository.save(notice);
        }
    }

    @Override
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    @Override
    public PageResponseDTO<NoticeDTO> getNotices(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("id");
        Page<Notice> result = noticeRepository.findAll(pageable);
        List<NoticeDTO> dtoList = result.getContent()
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return PageResponseDTO.<NoticeDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }


}
