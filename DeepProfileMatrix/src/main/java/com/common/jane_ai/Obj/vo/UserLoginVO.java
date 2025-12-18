package com.common.jane_ai.Obj.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class UserLoginVO {

    private Long userId;

    private String token;
}
