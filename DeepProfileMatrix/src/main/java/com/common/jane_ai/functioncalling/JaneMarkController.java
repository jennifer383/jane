package com.common.jane_ai.functioncalling;

import com.common.jane_ai.Obj.entity.PDFentity;
import com.common.jane_ai.mapper.PDFMapper;
import com.common.jane_ai.threadlocal.BaseParameterHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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
                              @ToolParam(description = "用户发出的用户名") String userName,
                              @ToolParam(description = "用户发出的密码") String password) {
        return markService.mark(score,userName,password);
    }
}
