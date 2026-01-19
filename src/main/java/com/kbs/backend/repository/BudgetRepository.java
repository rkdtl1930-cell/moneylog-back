package com.kbs.backend.repository;

import com.kbs.backend.domain.Budget;
import com.kbs.backend.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByMemberAndYearAndMonth(Member member, int year, int month);
    Optional<Budget> findByMember_IdAndYearAndMonth(Long mid, int year, int month);
    List<Budget> findByMember_IdOrderByYearDescMonthDesc(Long mid);
    Page<Budget> findByMember_Id(Long mid, Pageable pageable);
}
