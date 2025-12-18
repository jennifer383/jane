package com.common.jane_ai.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.jane_ai.Obj.dto.UserDTO;
import com.common.jane_ai.Obj.vo.UserLoginVO;
import com.common.jane_ai.Obj.entity.User;
import com.common.jane_ai.common.BCryptPasswordUtils;
import com.common.jane_ai.common.BaseThreadPool;
import com.common.jane_ai.common.BloomFilterUtils;
import com.common.jane_ai.enums.BaseCode;
import com.common.jane_ai.exception.JaneFrameworkException;

import com.common.jane_ai.jwt.JwtProperties;
import com.common.jane_ai.jwt.JwtUtils;
import com.common.jane_ai.mapper.UserMapper;
import com.common.jane_ai.service.AsyncService;
import com.common.jane_ai.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.common.jane_ai.constant.JaneRedisKeyConstant.JANE_USER_HAS_LOGIN;
import static com.common.jane_ai.constant.JaneTypeConstant.LOGIN;
import static com.common.jane_ai.constant.JaneTypeConstant.REGISTER;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private BCryptPasswordUtils bCryptPasswordUtils;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private BaseThreadPool baseThreadPool;
    @Autowired
    private AsyncService asyncService;

    @Autowired
    private BloomFilterUtils bloomFilterUtils;
    //1.维护userid的布隆过滤器
    //2.建立userid与username的映射关系  选择2，减少内存占用，资源利用率高，拓展性强

    @Override
    public UserLoginVO login(UserDTO uservo) {//使用bulong
        User user=getUserExist(uservo,LOGIN);
        //密码校验
        if(!BCryptPasswordUtils.check(uservo.getPassword(),user.getPassword())){
            throw new JaneFrameworkException(BaseCode.USER_LOGIN_PASSWORD_ERROR);
        }
//        baseThreadPool.execute(() -> {
//            asyncService.yureprepare(user.getId());
//        });
        //存在就生成token
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",user.getId());//userid作为参数当作数据体
        String jwt = JwtUtils.createJwt(jwtProperties.getUserTtl(), claims);
        //加入redis// 过期时间为token的过期时间
        redisTemplate.opsForValue().set(JANE_USER_HAS_LOGIN+user.getId(),
                user.getUsername(),jwtProperties.getUserTtl(), TimeUnit.MILLISECONDS);
        log.info("用户登录成功-username-{}",user.getUsername());
        return UserLoginVO.builder()
                .userId(user.getId())
                .token(jwt)
                .build();
    }

    @Override//注册用户时的两次密码输入使用前端进行校
    public void register(UserDTO userDTO) {
//Valid自动判断非空以及密码的校验
//        用户是否存在
        getUserExist(userDTO,REGISTER);
        //不存在就 加盐算法密码并插入
        String password = bCryptPasswordUtils.encrypt(userDTO.getPassword());
        User user=new User(userDTO.getUsername(),password);
        //加入用户的name因为登录和注册用的是用户名
        bloomFilterUtils.add(user.getUsername());
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            throw new JaneFrameworkException(BaseCode.USER_REGISTER_EXIST);
        }
        log.info("用户注册成功-username-{}",userDTO.getUsername());
    }

    /**
     * 查询用户是否存在
     * @param uservo
     * @param type: type为LOGIN 检测抛出用户不存在
     *             type为REGISTER 检测抛出用户已存在
     * @return
     */
    private User getUserExist(UserDTO uservo,int type) {
        //也可以使用bulong先判断username是否存在 少数误判才查询数据库
        // 以及前端可以按钮置灰  /说不是就不是
        boolean isExist = bloomFilterUtils.contains(uservo.getUsername());
        if(!isExist && type==LOGIN){
            throw new JaneFrameworkException(BaseCode.USER_LOGIN_UNEXIST);
        }else if(!isExist && type==REGISTER){
            return null;
        }
//        //无需密码校验
        //先查询用户是否存在
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, uservo.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        if(Objects.isNull(user) && type==LOGIN){
//  自定义异常+ 全局异常处理器
            throw new JaneFrameworkException(BaseCode.USER_LOGIN_UNEXIST);
        }
        if(!Objects.isNull(user) && type==REGISTER){
            throw new JaneFrameworkException(BaseCode.USER_REGISTER_EXIST);
        }
        return user;
    }
}
