package com.kbs.backend.repository;

import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUsername(String username);
    boolean existsByUsername(String username);

    @Modifying
    @Query("update Member set role=:role where username=:username")
    void updateMemberRole(@Param("username") String username, @Param("role") Role role);
}
