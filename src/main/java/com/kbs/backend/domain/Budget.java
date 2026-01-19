package com.kbs.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "mid")
    private Member member;
    @Column(nullable = false)
    private int month;
    @Column(nullable = false)
    private int year;
    @Column(nullable = false)
    private int limitAmount;
    private int usedAmount;
    @CreationTimestamp
    @DateTimeFormat( pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;

    public void updateLimitAmount(int newLimit){
        this.limitAmount = newLimit;
    }
    public void addUsedAmount(int amount){
        this.usedAmount += amount;
    }
    public void minusUsedAmount(int amount){
        this.usedAmount -= amount;
    }
    public void changeLimitAmount(int delta) {
        int newLimit = this.limitAmount + delta;

        if (newLimit < 0) {
            throw new IllegalStateException("예산은 0보다 작을 수 없습니다.");
        }

        this.limitAmount = newLimit;
    }
}
