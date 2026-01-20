package com.kbs.backend.repository.search;

import com.kbs.backend.domain.Board;
import com.kbs.backend.domain.QBoard;
import com.kbs.backend.domain.QMember;
import com.kbs.backend.dto.BoardDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class BoardSearchImpl extends QuerydslRepositorySupport implements BoardSearch {
    public BoardSearchImpl() { super(Board.class);}

    @Override
    public Page<BoardDTO> searchAll(String[] types, String keyword, Pageable pageable) {
        QBoard board = QBoard.board;
        QMember member = QMember.member;
        JPQLQuery<Board> query = from(board);
        query.leftJoin(board.member, member);
        if((types!=null && types.length>0) && keyword != null){
            BooleanBuilder builder = new BooleanBuilder();
            for(String type : types){
                switch (type){
                    case "t":
                        builder.or(board.title.contains(keyword));
                        break;
                    case "c":
                        builder.or(board.content.contains(keyword));
                        break;
                    case "w":
                        builder.or(member.nickname.contains(keyword));
                        break;
                }
            }
            query.where(builder);
        }
        query.where(board.id.gt(0l));
        JPQLQuery<BoardDTO> dtoQuery = query.select(
                Projections.bean(
                        BoardDTO.class,
                        board.id,
                        board.title,
                        board.content,
                        board.createTime,
                        board.updateTime,
                        board.member.nickname.as("nickname")
                )
        );
        this.getQuerydsl().applyPagination(pageable, dtoQuery);
        List<BoardDTO> list =dtoQuery.fetch();
        long count = query.fetchCount();
        return new PageImpl<>(list, pageable, count);
    }
}
