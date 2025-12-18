package com.common.jane_ai.Obj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.jane_ai.Obj.entity.base.BaseTableData;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")  //不然默认就是public
public class User extends BaseTableData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;
    //头像
    private String avatarUrl;

    private Boolean status;

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

}
