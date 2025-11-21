package com.nsuslab.member.entity;

import com.nsuslab.member.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "ROLE_USER";

    @Builder
    public Member(String loginId, String name, String password) {
        this.loginId = loginId;
        this.password = password;
    }

    public Member(Long id, String loginId, String password) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
    }

    public void passwordEncode(String encode) {
        this.password = encode;
    }
}
