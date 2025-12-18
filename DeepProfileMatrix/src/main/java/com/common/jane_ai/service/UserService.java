package com.common.jane_ai.service;

import com.common.jane_ai.Obj.dto.UserDTO;
import com.common.jane_ai.Obj.vo.UserLoginVO;

public interface UserService {

    /**
     * 生成token
     * @param userDTO
     * @return
     */
    UserLoginVO login(UserDTO userDTO);

    /**
     * 用户注册
     * @param userDTO
     * @return
     */
    void register(UserDTO userDTO);
}
