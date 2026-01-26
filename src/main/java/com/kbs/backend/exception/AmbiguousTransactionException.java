package com.kbs.backend.exception;

import com.kbs.backend.domain.Transaction;

import java.util.List;

public class AmbiguousTransactionException extends RuntimeException {
    private final List<Transaction> candidates;
    public AmbiguousTransactionException(List<Transaction> candidates) {
        this.candidates = candidates;
    }
    public List<Transaction> getCandidates() {
        return candidates;
    }
}
