package com.common.jane_ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.common.jane_ai.Obj.entity.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper extends BaseMapper<User> {

}
