package com.kbs.backend.repository;

import com.kbs.backend.domain.Notice;
import com.kbs.backend.repository.search.NoticeSearch;
import com.kbs.backend.service.NoticeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeSearch {
}
