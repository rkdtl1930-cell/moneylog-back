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
public class File implements Comparable<File> {
    @Id
    private String uuid;
    private String fileName;
    private int ord;
    private boolean image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="bno")
    private Board board;

    @Override
    public int compareTo(File other) {
        return this.ord - other.ord;
    }
}
