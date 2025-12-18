package com.common.jane_ai.rag;

import jodd.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig.Builder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MarkdownLoader 用于加载指定路径下的 Markdown 文件，并将其解析成 Document 片段。
 * 这个类使用 Spring 的 ResourcePatternResolver 来查找资源文件，
 * 并通过 Spring AI 提供的 MarkdownDocumentReader 来解析 Markdown 内容。
 */
@AllArgsConstructor  // Lombok 注解，自动生成带所有成员变量参数的构造函数
@Slf4j             // Lombok 注解，自动生成日志对象 log
public class MarkdownLoader {

    /**
     * 用于解析和查找资源文件的接口，Spring 提供的。
     * 通过它可以根据路径模式获取一组资源（比如 classpath 下面的文件）。
     */
    private final ResourcePatternResolver resourcePatternResolver;

    /**
     * 加载所有 Markdown 文件，解析并返回所有文档片段
     *
     * @return List<Document> 解析出的所有文档片段集合
     */
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();  // 存储所有文档片段的集合

        try {
            // 根据路径模式查找所有的 Markdown 文件，路径为 classpath:jane/*.md
            Resource[] resources = resourcePatternResolver.getResources("classpath:jane/*.md");
            log.info("找到 {} 个Markdown文件", resources.length);

            // 遍历每个 Markdown 文件资源
            for (Resource resource : resources) {
                String fileName = resource.getFilename();  // 获取文件名
                log.info("正在处理文件: {}", fileName);

                // 默认标签为文件名
                String label = fileName;

                // 如果文件名非空，尝试按 '-' 分割，并取第一个部分作为标签
                if (StringUtil.isNotEmpty(fileName)) {
                    final String[] parts = fileName.split("-");
                    if (parts.length > 1) {
                        label = parts[0];
                    }
                }
                log.info("提取的文档标签: {}", label);

                // 构建 MarkdownDocumentReader 的配置对象
                Builder builder = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)  // 使得水平线分割生成多个文档片段
                        .withIncludeCodeBlock(false)             // 不包含代码块内容
                        .withIncludeBlockquote(false);           // 不包含引用块内容

                // 添加文件名到额外的元数据中，方便后续处理
                if (StringUtil.isNotEmpty(fileName)) {
                    builder.withAdditionalMetadata("name", fileName);
                }

                // 添加提取的标签到额外的元数据中
                if (StringUtil.isNotEmpty(label)) {
                    builder.withAdditionalMetadata("label", label);
                }

                // 构建最终配置
                MarkdownDocumentReaderConfig config = builder.build();

                // 使用 MarkdownDocumentReader 解析资源文件，生成文档片段列表
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                //解析 Markdown 文件内容，并将其拆分成一个个 Document 对象
                List<Document> documents = markdownDocumentReader.get();

                log.info("文件 {} 加载了 {} 个文档片段", fileName, documents.size());

                // 将当前文件解析出的所有文档片段加入到总集合中
                allDocuments.addAll(documents);
            }

            log.info("总共加载了 {} 个文档片段", allDocuments.size());
        } catch (IOException e) {
            // 捕获加载资源或读取文件的异常，并记录错误日志
            log.error("Markdown 文档加载失败", e);
        }

        // 返回所有解析出的文档片段
        return allDocuments;
    }
    // Document 主要字段
    //字段名	类型	说明
    //id	String	文档唯一标识符，不能为空
    //text	String (nullable)	文本内容（当文档是文本时）
    //media	Media (nullable)	多媒体内容（当文档是非文本时）
    //metadata	Map<String, Object>	元数据，存储额外的属性信息
    //score	Double (nullable)	相关分数（例如相似度分数）
    //contentFormatter	ContentFormatter (ignored in JSON)	内容格式化器，用于格式化内容显示
}
