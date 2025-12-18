package com.common.jane_ai.Obj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.jane_ai.Obj.entity.base.BaseTableData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("jlpdf")
public class PDFentity extends BaseTableData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Double pdfScore;
    private String userName;
    private String password;




        @TableField("pdf_update_count")
    private Integer pdfUpdateCount;


    private Boolean status;

}
