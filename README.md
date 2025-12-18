
          
# Jane AI 项目文档

## 1. 技术栈

### 后端技术栈
| 技术/框架         | 版本    | 用途                 |
| ----------------- | ------- | -------------------- |
| Spring Boot       | 3.5.3   | 后端应用框架         |
| Java              | 17      | 开发语言             |
| Spring AI         | 1.0.0   | AI 模型集成框架      |
| MyBatis Plus      | 3.5.7   | ORM 框架             |
| Redis             | -       | 分布式缓存、会话管理 |
| MySQL             | 8.0.29  | 关系型数据库         |
| Caffeine          | 3.1.8   | 本地缓存             |
| JWT               | 0.12.5  | 身份认证             |
| SpringDoc OpenAPI | 2.7.0   | API 文档             |
| Lombok            | 1.18.22 | 代码简化工具         |
| Hutool            | 5.8.25  | Java 工具库          |
| FastJSON          | 1.2.83  | JSON 处理            |
| Spring AOP        | -       | 面向切面编程         |

### 前端技术栈
| 技术/框架                     | 版本    | 用途          |
| ----------------------------- | ------- | ------------- |
| Vue                           | 3.4.19  | 前端框架      |
| Vite                          | 5.1.4   | 构建工具      |
| Pinia                         | 2.1.7   | 状态管理      |
| Vue Router                    | 4.5.1   | 路由管理      |
| @fortawesome/fontawesome-free | 6.4.0   | 图标库        |
| highlight.js                  | 11.11.1 | 代码高亮      |
| marked                        | 16.1.1  | Markdown 解析 |

## 2. 项目亮点

1. **动态模型切换**
   - 支持多种 AI 模型（Ollama、OpenAI、DeepSeek）
   - 前后端联动，实时切换模型
   - 线程安全的模型管理机制

2. **高性能设计**
   - 两级缓存架构（Caffeine 本地缓存 + Redis 分布式缓存）
   - 异步编程支持
   - 速率限制保护

3. **现代化架构**
   - RESTful API 设计
   - 分层架构（Controller、Service、Mapper）
   - 完整的异常处理和日志记录
   - 基于策略模式的请求验证

4. **安全设计**
   - JWT 身份认证
   - 请求验证和过滤
   - 密码加密存储
   - CORS 安全配置

5. **丰富的 AI 功能**
   - 聊天功能
   - RAG（检索增强生成）
   - PDF 处理
   - 聊天历史记录管理

6. **用户友好的前端界面**
   - 现代化的 UI 设计
   - 响应式布局
   - 支持模型切换
   - 聊天历史记录查看

## 3. 核心功能特性

### 3.1 AI 聊天功能
- 支持与多种 AI 模型进行对话
- 实时消息响应
- 支持 Markdown 格式输出
- 代码高亮显示

### 3.2 模型切换功能
- 前端提供模型选择器组件
- 支持搜索和筛选模型
- 后端动态创建 ChatClient 实例
- 线程安全的模型切换机制

### 3.3 聊天历史记录
- 保存聊天历史到数据库
- 支持查看历史聊天记录
- 聊天记录分页查询
- 聊天历史标题生成

### 3.4 用户认证
- JWT 令牌生成和验证
- 密码加密存储
- 用户登录和注册
- 权限控制

### 3.5 PDF 处理
- PDF 上传和解析
- PDF 内容检索
- PDF 缓存管理

### 3.6 RAG 功能
- 检索增强生成
- Markdown 文档加载
- 向量数据库集成

### 3.7 缓存机制
- Caffeine 本地缓存
- Redis 分布式缓存
- 缓存过期策略
- 缓存预热

### 3.8 API 文档
- Swagger UI 接口文档
- 完整的 API 描述
- 支持在线接口测试

## 4. 启动说明

### 4.1 后端启动

#### 前置条件
- JDK 17 或以上版本
- MySQL 8.0.29 或以上版本
- Redis 服务运行中
- Maven 3.6 或以上版本

#### 启动步骤
1. **配置数据库**
   - 创建 MySQL 数据库 `jane_ai`
   - 执行 `DeepProfileMatrix/sql/DeepProfileMatrix.sql` 脚本初始化表结构

2. **配置 Redis**
   - 确保 Redis 服务运行在默认端口 6379
   - 如果需要修改 Redis 配置，编辑 `DeepProfileMatrix/src/main/resources/application.yml` 文件

3. **配置 AI 模型**
   - 在 `application.yml` 中配置 AI 模型的相关参数
   - 支持 Ollama、OpenAI、DeepSeek 等模型

4. **启动应用**
   ```bash
   cd DeepProfileMatrix
   mvn spring-boot:run
   ```

5. **验证启动成功**
   - 访问 `http://localhost:8080/swagger-ui/index.html` 查看 API 文档
   - 如果可以正常访问 Swagger UI，则后端启动成功

### 4.2 前端启动

#### 前置条件
- Node.js 16 或以上版本
- npm 或 yarn 包管理器

#### 启动步骤
1. **安装依赖**
   ```bash
   cd TalentLinkPortal
   npm install
   ```

2. **启动开发服务器**
   ```bash
   npm run dev
   ```

3. **访问前端应用**
   - 开发服务器启动后，访问 `http://localhost:5173` 查看前端应用
   - 如果可以正常访问前端界面，则前端启动成功

## 5. 使用说明

### 5.1 前端使用

#### 5.1.1 登录/注册
1. 访问前端应用 `http://localhost:5173`
2. 点击右上角的登录/注册按钮
3. 输入用户名和密码进行登录，或点击注册新用户

#### 5.1.2 聊天功能
1. 登录成功后，进入聊天界面
2. 在输入框中输入消息，点击发送按钮或按 Enter 键发送
3. 等待 AI 模型回复
4. 查看聊天记录，支持上下滚动查看历史消息

#### 5.1.3 模型切换
1. 点击聊天界面右上角的模型选择器图标
2. 在模型选择器中搜索或选择想要切换的模型
3. 点击模型名称进行切换
4. 切换成功后，新的聊天消息将使用所选模型

#### 5.1.4 查看聊天历史
1. 在左侧边栏查看聊天历史记录列表
2. 点击历史记录项查看详细聊天内容
3. 支持删除聊天历史记录

### 5.2 后端 API 使用

#### 5.2.1 Swagger UI
1. 访问 `http://localhost:8080/swagger-ui/index.html`
2. 在 Swagger UI 中查看所有可用的 API 接口
3. 点击接口名称查看详细信息，包括请求参数、响应格式等
4. 点击 "Try it out" 按钮进行在线接口测试

#### 5.2.2 主要 API 接口

| 接口路径             | 方法 | 功能描述         |
| -------------------- | ---- | ---------------- |
| `/api/chat`          | POST | 发送聊天消息     |
| `/api/jane_chat`     | POST | Jane 聊天接口    |
| `/api/change_model`  | POST | 切换 AI 模型     |
| `/api/chat_history`  | GET  | 获取聊天历史记录 |
| `/api/pdf/upload`    | POST | 上传 PDF 文件    |
| `/api/user/login`    | POST | 用户登录         |
| `/api/user/register` | POST | 用户注册         |

## 6. 项目结构

### 6.1 后端结构（DeepProfileMatrix）
```
├── src/main/java/com/common/jane_ai/
│   ├── Obj/                  # 数据传输对象和实体类
│   ├── RepateExecuteLimit/   # 重复执行限制
│   ├── advisor/              # AOP 通知
│   ├── caffine/              # Caffeine 缓存配置
│   ├── common/               # 公共工具类
│   ├── config/               # 配置类
│   ├── constant/             # 常量定义
│   ├── controller/           # 控制器
│   ├── enums/                # 枚举类
│   ├── exception/            # 异常类
│   ├── filter/               # 过滤器
│   ├── functioncalling/      # 函数调用
│   ├── jwt/                  # JWT 相关
│   ├── lua/                  # Lua 脚本
│   ├── mapper/               # MyBatis Mapper
│   ├── mybatisplus/          # MyBatis Plus 配置
│   ├── rag/                  # RAG 相关
│   ├── redis_chat_memory/    # Redis 聊天记忆
│   ├── service/              # 服务层
│   ├── threadlocal/          # ThreadLocal 相关
│   ├── xxl_job/              # XXL Job 相关
│   └── JaneAiApplication.java # 应用入口
├── src/main/resources/       # 资源文件
└── pom.xml                   # Maven 依赖配置
```

### 6.2 前端结构（TalentLinkPortal）
```
├── src/
│   ├── api/                  # API 调用
│   ├── assets/               # 静态资源
│   ├── components/           # Vue 组件
│   ├── config/               # 配置文件
│   ├── router/               # 路由配置
│   ├── stores/               # Pinia 状态管理
│   ├── styles/               # 样式文件
│   ├── views/                # 页面组件
│   ├── App.vue               # 根组件
│   └── main.js               # 应用入口
├── .env.development          # 开发环境配置
├── .env.production           # 生产环境配置
├── package.json              # npm 依赖配置
└── vite.config.js            # Vite 配置
```

## 7. 常见问题和解决方案

### 7.1 Swagger UI 访问失败
- 确保后端应用已成功启动
- 检查 `RequestFreeAllowStrategyImpl.java` 中是否包含 Swagger UI 路径
- 检查防火墙设置，确保端口 8080 开放

### 7.2 前端模型切换异常
- 检查前端控制台是否有错误信息
- 确保后端 `changeModel` API 正常工作
- 检查网络连接，确保前端可以访问后端 API

### 7.3 数据库连接失败
- 检查 MySQL 服务是否运行
- 检查 `application.yml` 中的数据库配置是否正确
- 确保数据库用户有足够的权限

### 7.4 Redis 连接失败
- 检查 Redis 服务是否运行
- 检查 `application.yml` 中的 Redis 配置是否正确
- 确保 Redis 端口 6379 开放

## 8. 总结

Jane AI 项目是一个功能丰富、架构现代化的 AI 聊天应用，支持多种 AI 模型切换，具有高性能、高安全性和良好的用户体验。项目采用前后端分离架构，后端基于 Spring Boot 3.5.3，前端基于 Vue 3.4.19，集成了多种 AI 模型和现代化的技术栈。

通过本项目，用户可以体验到：
- 与多种 AI 模型进行对话
- 动态切换 AI 模型
- 查看和管理聊天历史记录
- 上传和处理 PDF 文件
- 体验 RAG 功能

项目的设计和实现体现了现代化软件架构的最佳实践，包括分层架构、分布式缓存、线程安全设计、完整的异常处理和日志记录、RESTful API 设计等。这些特性使得项目具有良好的可扩展性、可维护性和性能表现。
