package com.common.jane_ai.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.jane_ai.Obj.dto.ChatHistoryDTO;
import com.common.jane_ai.Obj.entity.ChatHistory;
import com.common.jane_ai.Obj.entity.PDFentity;
import com.common.jane_ai.Obj.entity.User;
import com.common.jane_ai.Obj.vo.PDFVO;
import com.common.jane_ai.caffine.PDFlocalCache;
import com.common.jane_ai.caffine.RankingLocalCache;
import com.common.jane_ai.constant.JaneRedisKeyConstant;
import com.common.jane_ai.lua.ChatHistoryTitle;
import com.common.jane_ai.mapper.ChatHistoryTitleMapper;
import com.common.jane_ai.mapper.PDFMapper;
import com.common.jane_ai.mapper.UserMapper;
import com.common.jane_ai.redis_chat_memory.dialect.RedisChatMemoryRepositoryDialect;
import com.common.jane_ai.service.ChatHistoryService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.common.jane_ai.constant.JaneRedisKeyConstant.JANE_CONVERSATION_TITLE_USER_PREFIX;
import static com.common.jane_ai.constant.JaneRedisKeyConstant.JANE_RANKING;

@Component
@Slf4j
//Spring Task
public class TaskJob {

    @Autowired
    private ChatHistoryTitle chatHistoryTitle;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisChatMemoryRepositoryDialect redisChatMemoryRepositoryDialect;
    @Autowired
    private ChatHistoryTitleMapper chatHistoryTitleMapper;
    @Autowired
    private RankingLocalCache rankingLocalCache;
    @Autowired
    private PDFMapper pdfMapper;

//    @PostConstruct
//    public void init() {
//        log.info("初始化-ChatHistoryTitle");
//        syncDBfromRedis();
//    }

    // 每10分钟执行一次
    @Scheduled(cron = "0 0/10 * * * ?")
    /**
     * 每30分钟同步一次数据库，同步title
     * //如果有 1000 个用户，其中 999 都成功，最后一个失败了——全部数据都会被回滚，这就是大粒度事务的坑。
     */
    public void syncDBfromRedis(){
        log.info("开始同步数据库-ChatHistoryTitle");
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class).eq(User::getStatus, true);
        List<Long> users = userMapper.selectList(queryWrapper).stream()
                .map(User::getId).toList();
        for (Long userId : users) {
            //“粒度缩小、失败隔离”
            syncDBfromRedisWithId(userId);
        }
        log.info("同步数据库-All-ChatHistoryTitle完成");
    }

    @Scheduled(cron = "0 0 0 * * ?")//每晚上12点执行
    /**
     * 同步数据库到Caffine
     */
    public void syncCaffinefromPG(){
        rankingLocalCache.removeRanking(JANE_RANKING);
        LambdaQueryWrapper<PDFentity> wrapper = Wrappers.lambdaQuery(PDFentity.class)
                .eq(PDFentity::getStatus, true)
                .orderByDesc(PDFentity::getPdfScore)
                .last("limit 20");
        List<PDFentity> pdfentityList = pdfMapper.selectList(wrapper);
        List<PDFVO> pdfvos = pdfentityList.stream().map(PDFentity -> new PDFVO(PDFentity.getPdfScore(), PDFentity.getUserName(), PDFentity.getPdfUpdateCount(), PDFentity.getUpdateTime()))
                .toList();
        log.info("排行榜数据同步-pdfvos");
        rankingLocalCache.putRanking(JaneRedisKeyConstant.JANE_RANKING, pdfvos);
    }

    @Transactional//事务id粒度缩小，提高效率
    public void syncDBfromRedisWithId(Long userId){
        List<String> ids = redisChatMemoryRepositoryDialect.findUserAllChatIds(userId.toString());
        //先从本地缓存中获取
        if (ids.isEmpty()) return;
        String keypre = JANE_CONVERSATION_TITLE_USER_PREFIX+userId+":";
        LambdaQueryWrapper<ChatHistory> wrapper = Wrappers.lambdaQuery(ChatHistory.class)
                .eq(ChatHistory::getUserId, userId)
                .eq(ChatHistory::getStatus, true)
                .select(ChatHistory::getChatId);
        List<ChatHistory> DBids = chatHistoryTitleMapper.selectList(wrapper);
        ids.removeAll(DBids.stream().map(ChatHistory::getChatId).toList());
        if (ids.isEmpty()) return;
        log.info("用户 {} 有 {} 个新 chatId 需要同步", userId, ids.size());
        //lua脚本获取，减少网络交互
        List<ChatHistoryDTO> chatHistoryDTOS = chatHistoryTitle.chathostoryTitleOperate(List.of(keypre), ids.toArray(new String[0]));
        try {
            for (ChatHistoryDTO chatHistoryDTO : chatHistoryDTOS) {
                ChatHistory chatHistory = new ChatHistory();
                //更新数据库
                BeanUtils.copyProperties(chatHistoryDTO, chatHistory);
                chatHistory.setUserId(userId);
                chatHistoryTitleMapper.insert(chatHistory);
            }
        } catch (BeansException e) {
            log.error("用户 {} 同步失败: {}", userId, e.getMessage(), e);
        }
        log.info("同步数据库-ChatHistoryTitle完成-{}",userId);
    }












}
