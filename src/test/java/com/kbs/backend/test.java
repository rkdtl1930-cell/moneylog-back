package com.kbs.backend;

import com.kbs.backend.domain.Member;
import com.kbs.backend.repository.MemberRepository;
import com.kbs.backend.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
@SpringBootTest
public class test {
    @Autowired
    private RecurringExpenseService recurringExpenseService;

    @Autowired
    private RecurringExpenseRepository recurringExpenseRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void testProcessRecurringExpenses() {
        System.out.println("===== 테스트 시작 =====");

        // 테스트용 더미 데이터 추가 (원하면)
        Member member = memberRepository.findById(1L).orElse(null);
        if (member != null) {
            RecurringExpense testExpense = new RecurringExpense();
            testExpense.setMember(member);
            testExpense.setAmount(1000);
            testExpense.setCategory("테스트");
            testExpense.setMemo("테스트 지출");
            testExpense.setStartDate(LocalDate.of(2025, 12, 2));
            testExpense.setType(RecurringType.DAILY);
            recurringExpenseRepository.save(testExpense);
        }

        recurringExpenseService.processRecurringExpenses();

        System.out.println("===== 테스트 종료 =====");
    }
}
