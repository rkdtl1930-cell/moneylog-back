package com.kbs.backend.repository.search;

import com.kbs.backend.domain.Board;
import com.kbs.backend.dto.BoardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardSearch {
    Page<BoardDTO> searchAll(String[] types, String keyword, Pageable pageable);
}
