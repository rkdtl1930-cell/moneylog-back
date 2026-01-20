package com.kbs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDTO {
    private Long id;
    private Long bno;
    private Long mid;
    private String content;
    private boolean deleted;
    private String nickname;
}
