package com.common.jane_ai.Obj.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")//不能为""与"   "
    private String username;

    @Size(min = 5, max = 20, message = "密码长度必须在6-20之间")
    private String password;
}
