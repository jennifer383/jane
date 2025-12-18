package com.common.jane_ai.controller;

import com.common.jane_ai.Obj.dto.UserDTO;
import com.common.jane_ai.Obj.vo.UserLoginVO;
import com.common.jane_ai.RepateExecuteLimit.RepateExecuteLimit;
import com.common.jane_ai.common.ApiResponse;
import com.common.jane_ai.common.BloomFilterUtils;
import com.common.jane_ai.constant.JaneTypeConstant;
import com.common.jane_ai.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Slf4j
//@Validated//参数校验  validated用于有内部对象，包括valid
@RequestMapping("/jane")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/login")
    //普通用户登录
    public ApiResponse<UserLoginVO> login(@RequestBody @Valid UserDTO userDTO){
        log.info("用户登录-username：{}",userDTO.getUsername());
        return ApiResponse.ok(userService.login(userDTO));
    }

    @PostMapping("/register")
    @RepateExecuteLimit(type = JaneTypeConstant.REGISTER_MIDENG, userName = "#userDTO.username", durationTime = 600L)
    public ApiResponse register(@RequestBody @Valid UserDTO userDTO){
        log.info("用户注册-username：{}",userDTO.getUsername());
        userService.register(userDTO);
        return ApiResponse.ok();
    }
}
