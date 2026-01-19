package com.kbs.backend.repository;

import com.kbs.backend.domain.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByBoardIdAndDeletedFalse(Long bno);
    @Query("select r from Reply r where r.board.id=:bno")
    Page<Reply> listOfBoard(Long bno, Pageable pageable);
}
