package com.nsuslab.member.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LoginRequest {
    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Email(message = "아이디는 올바른 이메일 형식이어야 합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 4, max = 12, message = "비밀번호는 4~12자 사이여야 합니다.")
    private String password;

}
