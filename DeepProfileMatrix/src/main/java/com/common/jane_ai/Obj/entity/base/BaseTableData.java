package com.common.jane_ai.Obj.entity.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
//@Data 是 Lombok 提供的一个注解，用于自动生成类的 getter、setter、toString、equals
//和 hashCode 等方法，简化 Java Bean 的编写。加上该注解后，无需手动编写这些重复代码。
public class BaseTableData {

    //公共字段模板

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
