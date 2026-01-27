package com.kbs.backend.service;

import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Transaction;
import com.kbs.backend.domain.TransactionType;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.dto.TransactionCandidateDTO;
import com.kbs.backend.dto.TransactionDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TransactionService {

    Long register(TransactionDTO transactionDTO, Member member);
    TransactionDTO get(Long id);
    PageResponseDTO<TransactionDTO> getList(PageRequestDTO pageRequestDTO, Long mid);
    void modify(TransactionDTO transactionDTO);
    void remove(Long id);
    PageResponseDTO<TransactionDTO> getListByDate(PageRequestDTO pageRequestDTO, Long mid, LocalDate date);
    PageResponseDTO<TransactionDTO> getListByPeriod(PageRequestDTO pageRequestDTO, Long mid, LocalDate start, LocalDate end);
    List<Map<String, Object>> getCategoryStats(Long mid);
    List<Map<String, Object>> getMonthlyStats(Long mid);
    PageResponseDTO<TransactionDTO> getListBySingleDay(PageRequestDTO pageRequestDTO, Long mid, LocalDate date);
    void removeByPeriod(Long mid, LocalDate start, LocalDate end);
    void removeByAI(Long mid, LocalDate date, int amount, String memo, String type);
    void modifyByAI(Long mid, LocalDate date, int amount, String memo, TransactionDTO newData);
    void confirmDeleteByChat(Long mid, List<Integer> candidateIndexes, String expectedType);
    List<TransactionCandidateDTO> getCandidatesForUser(Long mid, LocalDate date, int amount, String memo);
    void storeDeleteCandidates(Long mid, List<TransactionCandidateDTO> dtos);
    List<TransactionCandidateDTO> getUpdateCandidates(Long mid, LocalDate date, Integer amount, String memo, String type);
    void confirmUpdateByChat(Long mid, String type, Integer candidateIndex, TransactionDTO newData);
    void storeUpdateCandidates(Long mid, List<TransactionCandidateDTO> dtos);
    PageResponseDTO<TransactionDTO> getListByAI(PageRequestDTO pageRequestDTO, Long mid, String type);


    default Transaction dtoToEntity(TransactionDTO transactionDTO, Member member) {
        Transaction transaction = Transaction.builder()
                .id(transactionDTO.getId())
                .member(member)
                .amount(transactionDTO.getAmount())
                .category(transactionDTO.getCategory())
                .memo(transactionDTO.getMemo())
                .date(transactionDTO.getDate())
                .type(TransactionType.valueOf(transactionDTO.getType()))
                .build();
        return transaction;
    }

    default TransactionDTO entityToDto(Transaction transaction) {
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .category(transaction.getCategory())
                .memo(transaction.getMemo())
                .date(transaction.getDate())
                .type(transaction.getType().name())
                .mid(transaction.getMember().getId())
                .build();
        return transactionDTO;
    }
}
