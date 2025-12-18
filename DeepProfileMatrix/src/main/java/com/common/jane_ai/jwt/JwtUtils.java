package com.common.jane_ai.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    // 指定签名的时候使用的签名算法，也就是header那部分
    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @Autowired
    private static JwtProperties jwtProperties;

    private static final String SecrateKey = "janeaichatjaneaichatjaneaichat2025072121929up";

    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
//     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJwt(long ttlMillis, Map<String,Object> claims) {

        // 生成JWT的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);


        // 设置jwt的body
        JwtBuilder builder = Jwts.builder()
                // 设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm,SecrateKey)
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .claims(claims)
                // 设置过期时间
                .expiration(exp);
        return builder.compact();//返回完整的JWT令牌字符串，格式为"xxxxx.yyyyy.zzzzz"
    }

    /**
     * Token解密
     *
//     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return
     */
    public static Map<String,Object> parseJWT(String token) {

        // 得到DefaultJwtParser
        JwtParser jwtParser = Jwts.parser()
                // 设置签名的秘钥
                .setSigningKey(SecrateKey)
                .build();
        Jws<Claims> jws = jwtParser.parseSignedClaims(token);
        Claims claims = jws.getPayload();
        return new HashMap<>(claims);//所以验证是否有效是通过是否有异常抛出来判断的，如果方法正常返回则表示验证通过。
    }
}//提取Header和Payload：从JWT中分离各部分
//重新计算签名：使用相同的算法和密钥对Header+Payload重新签名
//比较签名：将重新计算的签名与JWT中的签名进行比较
//验证结果：如果签名匹配，则验证通过
//2. 不是"解密"而是"验证"
