package com.common.jane_ai.filter.StrategyImpl;

import com.common.jane_ai.common.ResponseUtils;
import com.common.jane_ai.constant.JaneRedisKeyConstant;
import com.common.jane_ai.enums.BaseCode;
import com.common.jane_ai.filter.RequestValidationStrategy;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.common.jane_ai.constant.JaneTypeConstant.CHAT_ID;
import static com.common.jane_ai.constant.JaneTypeConstant.USER_ID;

@Component
@Slf4j
public class RequestAYouKeStrategyImpl implements RequestValidationStrategy {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final List<String> YOUKE_PATTERNS = List.of(
            "/jane/chat"//回话00
            ,"/jane/model"
//            ,"/jane/titlehistory"//会话历史

    );
    //游客最多使用10个会话
    @Override
    public boolean matching(String Path) {
        String userIdStr = BaseParameterHolder.getBaseParameter(USER_ID);
        if(YOUKE_PATTERNS.contains(Path) && (userIdStr == null || userIdStr.isEmpty() || Integer.parseInt(userIdStr)==0)){
            return true;
        }
        return false;
    }

    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path=request.getRequestURI();
//        if(path.equals("/jane/t")){
//            ResponseUtils.writeError(response, BaseCode.YOUKE_CANT_TITLE_EXCEPTION);
//            return false;
//        }
        
        // 只有聊天请求才需要检查会话次数
        if("/jane/chat".equals(path)) {
            //使用redis处理最多只给10个会话
            String chatIdStr = request.getParameter(CHAT_ID);
            
            // 检查chatId是否为空
            if(StringUtils.isEmpty(chatIdStr)) {
                log.info("YOUKE_STRATEGY-Acess-聊天请求无chatId");
                return true;
            }
            
            String key= JaneRedisKeyConstant.JANE_YOUKE_TALK_NUM + chatIdStr;
            String numStr = redisTemplate.opsForValue().get(key);
            if(StringUtils.isEmpty(numStr)){ //不存在
                //信息只保存24h
                redisTemplate.opsForValue().set(key, "1", 60*60*24, TimeUnit.SECONDS);
                log.info("YOUKE_STRATEGY-Acess-可用-chatId:{}",chatIdStr);
            }else if(Integer.parseInt(numStr)>=10){
                ResponseUtils.writeError(response, BaseCode.YOUKE_CHAT_NUM_LIMIT_EXCEPTION);
                return false;
            }else{
                redisTemplate.opsForValue().increment(key);
            }
        }
        return true;
    }
}
