package com.kbs.backend.repository.search;

import com.kbs.backend.domain.Notice;
import com.kbs.backend.domain.QNotice;
import com.kbs.backend.dto.NoticeDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class NoticeSearchImpl extends QuerydslRepositorySupport implements NoticeSearch {
    public NoticeSearchImpl() { super(Notice.class); }

    @Override
    public Page<NoticeDTO> searchAll(String[] types, String keyword, Pageable pageable) {
        QNotice notice = QNotice.notice;
        JPQLQuery<Notice> query = from(notice);
        if((types != null && types.length > 0) && keyword != null) {
            BooleanBuilder builder = new BooleanBuilder();
            for(String type : types) {
                switch(type){
                    case "t":
                        builder.or(notice.title.containsIgnoreCase(keyword));
                        break;
                    case "c":
                        builder.or(notice.content.containsIgnoreCase(keyword));
                        break;
                }
            }
            query.where(builder);
        }
        query.where(notice.id.gt(0l));
        JPQLQuery<NoticeDTO> dtoQuery = query.select(
                Projections.bean(
                        NoticeDTO.class,
                        notice.id,
                        notice.title,
                        notice.content,
                        notice.createTime
                )
        );
        this.getQuerydsl().applyPagination(pageable, dtoQuery);
        List<NoticeDTO> list = dtoQuery.fetch();
        long count = query.fetchCount();
        return new PageImpl<>(list, pageable, count);
    }
}
