package com.common.jane_ai.functioncalling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.common.jane_ai.constant.JaneTypeConstant.USER_ID;

@Slf4j
@Component
public class JaneMarkController {

    @Autowired
    private MarkServiceImpl markService;

    @Tool(description = "将用户的简历打分后，如果用户同意，就执行这个方法将分数上传至数据库")
    public String uploadScore(@ToolParam(description = "你分析用户简历后的取得的分数") Double score,
                              ToolContext toolContext) {
        // 身份由服务端注入，不作为模型可填写的工具参数。
        Object identity = toolContext == null ? null : toolContext.getContext().get(USER_ID);
        if (identity == null) {
            return "上传失败，请先登录 Jane 系统";
        }
        long userId;
        try {
            userId = Long.parseLong(identity.toString());
        } catch (NumberFormatException exception) {
            return "上传失败，登录身份无效，请重新登录";
        }
        return markService.mark(score, userId);
    }
}
