package com.common.jane_ai.Obj.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PDFVO {

    private Double pdfScore;

    private String userName;

    private Integer pdfUpdateCount;

    private LocalDateTime updateTime;
}
