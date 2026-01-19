package com.kbs.backend.repository.search;

import com.kbs.backend.dto.NoticeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeSearch {
    Page<NoticeDTO> searchAll(String[] types, String keyword, Pageable pageable);
}
