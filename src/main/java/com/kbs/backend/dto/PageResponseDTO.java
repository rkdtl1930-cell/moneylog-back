package com.kbs.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class PageResponseDTO<E> {
    private int page;
    private int size;
    private int total;
    private int pageBlockSize;
    private int start;
    private int end;
    private boolean prev;
    private boolean next;
    private int first;
    private int last;
    private List<E> dtoList;

    @Builder(builderMethodName = "withAll")
    public PageResponseDTO(PageRequestDTO pageRequestDTO, List<E> dtoList, int total) {
        this.page = pageRequestDTO.getPage();
        this.size = pageRequestDTO.getSize();
        this.total = total;
        this.pageBlockSize = 3;
        this.dtoList = dtoList;
        this.first = 1;
        this.last = Math.max(1, (int)Math.ceil((double)total/this.size));
        int tempEnd = (int)(Math.ceil(this.page/(double)pageBlockSize)*pageBlockSize);
        this.start = tempEnd - (pageBlockSize - 1);
        this.end = Math.min(tempEnd, this.last);

        if (this.start < 1) this.start = 1;
        this.prev = this.start > 1;
        this.next = this.end < this.last;
    }
}
