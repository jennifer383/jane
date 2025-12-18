package com.common.jane_ai.service;

import com.common.jane_ai.common.BloomFilterUtils;
import org.springframework.stereotype.Service;

@Service
public interface ModelService {
    /**
     * 切换模型
     * @param modelName 模型名称
     * @return 是否切换成功
     */
    boolean changeModel(String modelName);
    
    /**
     * 获取当前模型
     * @return 当前模型名称
     */
    String getCurrentModel();
}
