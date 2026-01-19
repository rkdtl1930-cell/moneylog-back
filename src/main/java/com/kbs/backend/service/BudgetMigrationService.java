package com.kbs.backend.service;

import com.kbs.backend.domain.Budget;
import com.kbs.backend.domain.Transaction;
import com.kbs.backend.domain.TransactionType;
import com.kbs.backend.repository.BudgetRepository;
import com.kbs.backend.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Log4j2
public class BudgetMigrationService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private static final int BATCH_SIZE = 100;

    @PostConstruct
    public void migrateExistingTransactions() {
        long totalTransactions = transactionRepository.count();
        int totalPages = (int) Math.ceil((double) totalTransactions / BATCH_SIZE);

        budgetRepository.findAll().forEach(b -> {
            b.setUsedAmount(0);
            budgetRepository.save(b);
        });

        for (int page = 0; page < totalPages; page++) {
            PageRequest pageable = PageRequest.of(page, BATCH_SIZE);
            List<Transaction> batch = transactionRepository.findBatch(pageable);

            batch.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .forEach(t -> {
                        LocalDate date = t.getDate();
                        int year = date.getYear();
                        int month = date.getMonthValue();

                        Budget budget = budgetRepository.findByMemberAndYearAndMonth(t.getMember(), year, month)
                                .orElseGet(() -> {
                                    Budget b = new Budget();
                                    b.setMember(t.getMember());
                                    b.setYear(year);
                                    b.setMonth(month);
                                    b.setLimitAmount(0);
                                    b.setUsedAmount(0);
                                    return budgetRepository.save(b);
                                });

                        budget.addUsedAmount(t.getAmount());
                        budgetRepository.save(budget);
                        log.info("Budget 업데이트 완료: member={}, year={}, month={}, addedAmount={}",
                                t.getMember().getId(), year, month, t.getAmount());
                    });

            log.info("Migration batch {} 완료", page + 1);
        }

        log.info("기존 Transaction 기반 Budget usedAmount 마이그레이션 완료!");
    }
}
