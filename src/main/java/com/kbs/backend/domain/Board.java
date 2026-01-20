package com.kbs.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 2000)
    private String content;
    @CreationTimestamp
    @DateTimeFormat( pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;
    @UpdateTimestamp
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mid", nullable = false)
    private Member member;
    private int readcount;
    @Column(length = 500)
    private String imageUrl;

//    @OneToMany(mappedBy="board", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
//    @Builder.Default
//    private List<File> files = new ArrayList<>();

    @OneToMany(mappedBy="board", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<Reply> replies = new ArrayList<>();

    public void updateReadCount(){ this.readcount+=1; }
}
