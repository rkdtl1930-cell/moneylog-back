package com.kbs.backend.service;

import com.kbs.backend.domain.Budget;
import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Transaction;
import com.kbs.backend.domain.TransactionType;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.dto.TransactionCandidateDTO;
import com.kbs.backend.dto.TransactionDTO;
import com.kbs.backend.exception.AmbiguousTransactionException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private BudgetRepository budgetRepository;

    private final Map<Long, List<TransactionCandidateDTO>> userCandidates = new HashMap<>();

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

    @Override
    public void removeByPeriod(Long mid, LocalDate start, LocalDate end) {
        if(start.isAfter(end)) {
            throw new IllegalArgumentException("start날짜가 end날짜보다 클 수 없습니다.");
        }
        transactionRepository.deleteByMember_IdAndDateBetween(mid, start, end);
    }

    @Override
    @Transactional
    public void removeByAI(Long mid, LocalDate date, int amount, String memo) {
        // 날짜로 먼저 조회
        List<Transaction> list = transactionRepository.findByMember_IdAndDate(mid, date);

        // 금액 조건 적용 (0이면 무시)
        if(amount > 0) {
            list = list.stream()
                    .filter(tx -> tx.getAmount() == amount)
                    .toList();
        }

        // memo 조건 적용 (빈 문자열이면 무시)
        if(memo != null && !memo.isEmpty()) {
            list = list.stream()
                    .filter(tx -> tx.getMemo() != null && tx.getMemo().contains(memo))
                    .toList();
        }

        if(list.isEmpty()) {
            throw new IllegalArgumentException("해당 거래 없음");
        }
        if(list.size() > 1) {
            throw new AmbiguousTransactionException(list); // 후보 리스트 반환
        }

        transactionRepository.delete(list.get(0));
    }

    @Override
    @Transactional
    public void modifyByAI(Long mid, LocalDate date, int amount, String memo, TransactionDTO newData) {
        List<Transaction> list = transactionRepository.findByMember_IdAndDateAndAmountAndMemoContaining(mid, date, amount, memo==null?"":memo);
        if(list.isEmpty()) {
            throw new IllegalArgumentException("해당 거래 없음");
        }
        if(list.size() > 1) {
            throw new AmbiguousTransactionException(list);
        }
        Transaction transaction = list.get(0);

        transaction.setAmount(newData.getAmount());
        transaction.setDate(newData.getDate());
        transaction.setMemo(newData.getMemo());
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void confirmDeleteByChat(Long mid, List<Integer> candidateIndexes) {
        List<TransactionCandidateDTO> candidates = userCandidates.get(mid);
        if(candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("삭제 후보 없음");
        }
        for(Integer idx : candidateIndexes) {
            if(idx > 0 && idx <= candidates.size()) {
                Long txId = candidates.get(idx-1).getId();
                transactionRepository.deleteById(txId);
            }
        }
        userCandidates.remove(mid);
    }

    @Override
    public List<TransactionCandidateDTO> getCandidatesForUser(Long mid, LocalDate date, int amount, String memo) {
        return userCandidates.getOrDefault(mid, Collections.emptyList());
    }

    @Override
    public void storeDeleteCandidates(Long mid, List<TransactionCandidateDTO> dtos) {
        userCandidates.put(mid, dtos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionCandidateDTO> getUpdateCandidates(Long mid, LocalDate date, Integer amount, String memo) {

        System.out.println(">>> mid = " + mid);
        System.out.println(">>> cond date = " + date);
        System.out.println(">>> cond amount = " + amount);
        System.out.println(">>> cond memo = [" + memo + "]");

        // 1) 무조건 전체 가져오기
        List<Transaction> list = transactionRepository.findByMember_Id(mid);

        System.out.println(">>> total tx count = " + list.size());

        for (Transaction tx : list) {
            System.out.println(
                    "TX id=" + tx.getId()
                            + ", date=" + tx.getDate()
                            + ", amount=" + tx.getAmount()
                            + ", memo=" + tx.getMemo()
            );
        }

        // 2) OR 필터
        List<Transaction> filtered = list.stream()
                .filter(tx -> {
                    boolean ok = false;

                    // 날짜 조건
                    if (date != null && tx.getDate() != null) {
                        boolean dateMatch = tx.getDate().isEqual(date);
                        System.out.println(
                                "COMPARE DATE: tx.id=" + tx.getId()
                                        + " tx.date=" + tx.getDate()
                                        + " vs cond.date=" + date
                                        + " => " + dateMatch
                        );
                        if (dateMatch) ok = true;
                    }

                    // 금액 조건
                    if (amount != null && amount > 0) {
                        boolean amountMatch = tx.getAmount() == amount;
                        System.out.println(
                                "COMPARE AMOUNT: tx.id=" + tx.getId()
                                        + " tx.amount=" + tx.getAmount()
                                        + " vs cond.amount=" + amount
                                        + " => " + amountMatch
                        );
                        if (amountMatch) ok = true;
                    }

                    // 메모 조건
                    if (memo != null && !memo.isBlank() && tx.getMemo() != null) {
                        boolean memoMatch = tx.getMemo().contains(memo);
                        System.out.println(
                                "COMPARE MEMO: tx.id=" + tx.getId()
                                        + " tx.memo=" + tx.getMemo()
                                        + " vs cond.memo=" + memo
                                        + " => " + memoMatch
                        );
                        if (memoMatch) ok = true;
                    }

                    System.out.println(">>> FINAL OK for tx.id=" + tx.getId() + " => " + ok);
                    return ok;
                })
                .toList();

        System.out.println(">>> filtered size = " + filtered.size());

        // 3) DTO 변환
        AtomicInteger seq = new AtomicInteger(1);
        List<TransactionCandidateDTO> dtos = filtered.stream()
                .map(tx -> new TransactionCandidateDTO(
                        tx.getId(),
                        tx.getDate(),
                        tx.getAmount(),
                        tx.getMemo(),
                        tx.getCategory(),
                        seq.getAndIncrement()
                ))
                .toList();

        // 4) 후보 저장
        storeUpdateCandidates(mid, dtos);

        System.out.println(">>> return dtos size = " + dtos.size());

        System.out.println(">>> RETURNING dtos size = " + dtos.size());


        return dtos;
    }

    @Override
    @Transactional
    public void confirmUpdateByChat(Long mid, Integer candidateIndex, TransactionDTO newData) {
        List<TransactionCandidateDTO> candidates = userCandidates.get(mid);

        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("수정 후보가 없습니다.");
        }

        if (candidateIndex == null) {
            throw new IllegalArgumentException("후보 번호가 없습니다.");
        }

        if (candidateIndex < 1 || candidateIndex > candidates.size()) {
            throw new IllegalArgumentException("잘못된 후보 번호: " + candidateIndex);
        }

        TransactionCandidateDTO selected = candidates.get(candidateIndex - 1);

        Transaction tx = transactionRepository.findById(selected.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "거래 ID 없음: " + selected.getId()));

        // ✅ 날짜 / 금액 / 메모만 수정 허용
        if (newData.getDate() != null) {
            tx.setDate(newData.getDate());
        }

        if (newData.getAmount() != null) {
            tx.setAmount(newData.getAmount());
        }

        if (newData.getMemo() != null) {
            tx.setMemo(newData.getMemo());
        }

        transactionRepository.save(tx);

        // ✅ confirm 끝나면 후보 상태 제거
        userCandidates.remove(mid);
    }

    @Override
    public void storeUpdateCandidates(Long mid, List<TransactionCandidateDTO> dtos) {
        userCandidates.put(mid, dtos);
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
