package com.nsuslab.member.repository;

import com.nsuslab.member.entity.Member;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.nsuslab.member.entity.QMember.member;


@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<Member> findByLoginId(String loginId) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(Member.class,
                        member.id,
                        member.loginId,
                        member.password
                ))
                .from(member)
                .where(member.loginId.in(loginId))
                .fetchOne());

    }

}
