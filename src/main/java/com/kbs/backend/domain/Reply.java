package com.kbs.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="mid")
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="bno")
    private Board board;
    @Column(nullable = false, length = 3000)
    private String content;
    private boolean deleted;
}
