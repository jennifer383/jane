package com.common.jane_ai.common;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BCryptPasswordUtils {

    //Blowfish + 多轮哈希加密
    //不可逆的哈希算法，所以它没有“解密”过程，只有“校验”过程

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//BCrypt设计不允许反推明文密码，只有通过“同样参数重新哈希再比对”的方式验证密码。
    //明文密码 + 数据库提取的盐 + 轮次
    //       ↓
    //重新计算 BCrypt 哈希
    //       ↓
    //结果和数据库哈希对比
    //       ↓
    //是否匹配 = 密码是否正确
    /**
     * 加密密码（自动加盐）
     * @param rawPassword 明文密码
     * @return 加密后的密码（含盐）
     */
    public static String encrypt(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     * @param inputPassword 用户输入的密码（明文）
     * @param hashedPassword 数据库存储的加密密码（含盐）
     * @return 是否匹配
     */
    public static boolean check(String inputPassword, String hashedPassword) {
        return StringUtils.hasText(inputPassword)
                && StringUtils.hasText(hashedPassword)
                && encoder.matches(inputPassword, hashedPassword);
    }

//    public static void main(String[] args) {
//        System.out.println(check("admin","$2a$10$tJ.3nhkfU/CJDjq239knteDh13SjgNtq.qc1cb./3KXt6YVYdVhKK"));
//    }
}
