package com.kbs.backend.service;

import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Transaction;
import com.kbs.backend.domain.TransactionType;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
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
