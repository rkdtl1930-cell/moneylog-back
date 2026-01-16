package com.kbs.backend.repository;

import com.kbs.backend.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByMember_Id(Long mid, Pageable pageable);
    Page<Transaction> findByMember_IdAndDate(Long mid, LocalDate date, Pageable pageable);
    Page<Transaction> findByMember_IdAndDateBetween(Long mid, LocalDate start, LocalDate end, Pageable pageable);

    @Query("SELECT t.category AS category, SUM(t.amount) AS total " +
            "FROM Transaction t WHERE t.member.id = :mid AND t.type = 'EXPENSE' " +
            "GROUP BY t.category")
    List<Map<String, Object>> sumByCategory(@Param("mid") Long mid);
    @Query("SELECT FUNCTION('DATE_FORMAT', t.date, '%Y-%m') AS month, " +
            "SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS income, " +
            "SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS expense " +
            "FROM Transaction t WHERE t.member.id = :mid " +
            "GROUP BY FUNCTION('DATE_FORMAT', t.date, '%Y-%m') ORDER BY month")
    List<Map<String, Object>> sumByMonth(@Param("mid") Long mid);

    @Query("SELECT t FROM Transaction t ORDER BY t.id")
    List<Transaction> findBatch(Pageable pageable);

}
