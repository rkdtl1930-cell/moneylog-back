package com.kbs.backend.service;

import com.kbs.backend.domain.Budget;
import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Transaction;
import com.kbs.backend.domain.TransactionType;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.dto.TransactionDTO;
import com.kbs.backend.repository.BudgetRepository;
import com.kbs.backend.repository.MemberRepository;
import com.kbs.backend.repository.TransactionRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private BudgetRepository budgetRepository;

    @Override
    public Long register(TransactionDTO transactionDTO, Member member) {
        Transaction transaction = dtoToEntity(transactionDTO, member);
        LocalDate date = transaction.getDate();
        int year = date.getYear();
        int month = date.getMonthValue();
        return transactionRepository.save(transaction).getId();
    }

    @Override
    public TransactionDTO get(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElse(null);
        return entityToDto(transaction);
    }

    @Override
    public PageResponseDTO<TransactionDTO> getList(PageRequestDTO pageRequestDTO, Long mid) {
        Pageable pageable = pageRequestDTO.getPageable("date");
        Page<Transaction> result = transactionRepository.findByMember_Id(mid, pageable);
        List<TransactionDTO> dtoList = result
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return PageResponseDTO.<TransactionDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }

    @Override
    public void modify(TransactionDTO transactionDTO) {
        Transaction transaction = transactionRepository.findById(transactionDTO.getId()).orElse(null);
        LocalDate date = transaction.getDate();
        int year = date.getYear();
        int month = date.getMonthValue();
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setDate(transactionDTO.getDate());
        transaction.setMemo(transactionDTO.getMemo());
        transaction.setCategory(transactionDTO.getCategory());
        transaction.setType(TransactionType.valueOf(transactionDTO.getType()));
        transactionRepository.save(transaction);
    }

    @Override
    public void remove(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElse(null);
        LocalDate date = transaction.getDate();
        int year = date.getYear();
        int month = date.getMonthValue();
        transactionRepository.deleteById(id);
    }

    @Override
    public PageResponseDTO<TransactionDTO> getListByDate(PageRequestDTO pageRequestDTO, Long mid, LocalDate date) {
        Pageable pageable = pageRequestDTO.getPageable("date");
        Page<Transaction> result = transactionRepository.findByMember_IdAndDate(mid, date, pageable);
        List<TransactionDTO> dtoList = result.getContent()
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return PageResponseDTO.<TransactionDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }

    @Override
    public PageResponseDTO<TransactionDTO> getListByPeriod(PageRequestDTO pageRequestDTO, Long mid, LocalDate start, LocalDate end) {
        Pageable pageable = pageRequestDTO.getPageable("date");
        Page<Transaction> result = transactionRepository.findByMember_IdAndDateBetween(mid, start, end, pageable);
        List<TransactionDTO> dtoList = result.getContent()
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return PageResponseDTO.<TransactionDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int)result.getTotalElements())
                .build();
    }

    @Override
    public List<Map<String, Object>> getCategoryStats(Long mid) {
        return transactionRepository.sumByCategory(mid);
    }

    @Override
    public List<Map<String, Object>> getMonthlyStats(Long mid) {
        return transactionRepository.sumByMonth(mid);
    }

    @Override
    public PageResponseDTO<TransactionDTO> getListBySingleDay(PageRequestDTO pageRequestDTO, Long mid, LocalDate date) {
        Pageable pageable = pageRequestDTO.getPageable("date");
        Page<Transaction> result =
                transactionRepository.findByMember_IdAndDate(mid, date, pageable);
        List<TransactionDTO> dtoList = result.getContent()
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return PageResponseDTO.<TransactionDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    @Transactional
    public void migrateUsedAmountFromExistingTransactions(){
        List<Budget> allBudgets= budgetRepository.findAll();
        for(Budget b : allBudgets){
            b.setUsedAmount(0);
        }
        budgetRepository.saveAll(allBudgets);
        List<Transaction> allTransactions= transactionRepository.findAll();
        for(Transaction t : allTransactions){
            if(t.getType() != TransactionType.EXPENSE){
                continue;
            }
            int year = t.getDate().getYear();
            int month = t.getDate().getMonthValue();
            Budget budget = budgetRepository.findByMember_IdAndYearAndMonth(t.getMember().getId(),year,month)
                    .orElseGet(()->{
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
        }
    }
}
