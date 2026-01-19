package com.kbs.backend.service;

import com.kbs.backend.domain.Budget;
import com.kbs.backend.domain.Member;
import com.kbs.backend.dto.BudgetDTO;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.repository.BudgetRepository;
import com.kbs.backend.repository.MemberRepository;
import com.kbs.backend.security.UserPrincipal;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Log4j2
public class BudgetServiceImpl implements BudgetService {
    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public BudgetDTO getOrCreateMonthlyBudget(Long mid, int defaultLimit) {
        Member member = memberRepository.findById(mid).orElse(null);
        LocalDate now =  LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        Budget budget = budgetRepository.findByMemberAndYearAndMonth(member, year, month)
                .orElseGet(() -> {
                    Budget newBudget = Budget.builder()
                            .member(member)
                            .year(year)
                            .month(month)
                            .limitAmount(defaultLimit)
                            .usedAmount(0)
                            .build();
                    return budgetRepository.save(newBudget);
                });
        return entityToDto(budget);
    }

    @Override
    @Transactional
    public void addExpense(Long mid, int amount, int defaultLimit) {
        Member member = memberRepository.findById(mid).orElse(null);
        LocalDate now =  LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        Budget budget = budgetRepository.findByMemberAndYearAndMonth(member, year, month)
                .orElseGet(() -> {
                    Budget newBudget = Budget.builder()
                            .member(member)
                            .year(year)
                            .month(month)
                            .limitAmount(defaultLimit)
                            .usedAmount(0)
                            .build();
                    return budgetRepository.save(newBudget);
                });
        budget.addUsedAmount(amount);
        budgetRepository.save(budget);
    }

    @Override
    @Transactional
    public void updateLimit(Long mid, int newLimit) {
        System.out.println(">>> updateLimit() mid = " + mid);
        LocalDate now =  LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        Budget budget = budgetRepository.findByMember_IdAndYearAndMonth(mid, year, month)
                .orElseThrow(() -> new IllegalStateException("이번 달 예산이 존재하지 않습니다."));
        budget.setLimitAmount(newLimit);
        budgetRepository.save(budget);
    }

    @Override
    @Transactional
    public void resetMonthlyBudgets(List<Member> members, int defaultLimit) {
        LocalDate now =  LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int BATCH_SIZE = 100;
        for (int i = 0; i < members.size(); i += BATCH_SIZE) {
            List<Member> batch = members.subList(i, Math.min(i + BATCH_SIZE, members.size()));

            batch.forEach(member -> {
                budgetRepository.findByMemberAndYearAndMonth(member, year, month)
                        .orElseGet(() -> {
                            Budget budget = Budget.builder()
                                    .member(member)
                                    .year(year)
                                    .month(month)
                                    .limitAmount(defaultLimit)
                                    .usedAmount(0)
                                    .build();
                            return budgetRepository.save(budget);
                        });
            });
            log.info("Budget reset batch {} 완료", (i / BATCH_SIZE) + 1);
        }
    }

    @Override
    public PageResponseDTO<BudgetDTO> getBudgets(PageRequestDTO pageRequestDTO, Long mid) {
        Pageable pageable = pageRequestDTO.getPageable("year","month");
        Page<Budget> result = budgetRepository.findByMember_Id(mid,pageable);
        List<BudgetDTO> dtoList = result.getContent()
                .stream()
                .map(this::entityToDto)
                .toList();
        return PageResponseDTO.<BudgetDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }

    @Override
    public void createBudget(BudgetDTO budgetDTO) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long mid = userPrincipal.getId();
        Budget budget = dtoToEntity(budgetDTO);
        Member member = memberRepository.findById(mid).orElse(null);
        budget.setMember(member);
        budgetRepository.save(budget);
    }
}
