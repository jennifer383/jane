package com.common.jane_ai.service.impl;

import com.common.jane_ai.service.ModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 模型管理服务实现类
 * 用于管理和切换当前使用的AI模型
 */
@Slf4j
@Service
public class ModelServiceImpl implements ModelService {

    // 当前使用的模型名称
    private volatile String currentModel = "qwen-turbo";

    /**
     * 切换模型
     * @param modelName 模型名称
     * @return 是否切换成功
     */
    @Override
    public boolean changeModel(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            log.error("模型名称不能为空");
            return false;
        }

        try {
            // 检查模型名称是否有效
            if (!isValidModel(modelName)) {
                log.error("无效的模型名称: {}", modelName);
                return false;
            }

            // 切换模型
            currentModel = modelName;
            log.info("模型切换成功，当前模型: {}", currentModel);
            return true;
        } catch (Exception e) {
            log.error("模型切换失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前模型
     * @return 当前模型名称
     */
    @Override
    public String getCurrentModel() {
        return currentModel;
    }

    /**
     * 检查模型名称是否有效
     * @param modelName 模型名称
     * @return 是否有效
     */
    private boolean isValidModel(String modelName) {
        // 支持的模型列表
        return modelName.equals("qwen-turbo") ||
               modelName.equals("gpt-4-turbo") ||
               modelName.equals("claude-3-opus") ||
               modelName.equals("gemini-pro") ||
               modelName.equals("llama3.2:3b") ||
               modelName.equals("deepseek-chat") ||
               modelName.equals("kimi-k2-thinking");
    }
}
