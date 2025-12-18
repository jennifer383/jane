# JaneAI 前端本地部署与端口说明

本文仅面向前端，聚焦本地部署注意事项，并明确前后端端口的对应关系，便于正确联通接口。

## 1. 端口与地址约定

- 前端开发服务（默认）：http://localhost:8080
- 后端 API 服务（默认）：http://localhost:8081

说明：
- 后端地址由环境变量 VITE_API_BASE_URL 决定，前端通过 src/config/api.js 读取并拼接接口路径（直连后端，不依赖 /api 代理）。
- 如果后端端口或地址发生变化，前端只需更新 .env.development 中的 VITE_API_BASE_URL。

> 当前项目配置与实现：
> - vite.config.js 已设置 server.port = 8080 且 strictPort = true（端口被占用时不会自动切换）。
> - src/config/api.js 使用 VITE_API_BASE_URL（默认 http://localhost:8081）直连后端。
> - vite.config.js 中虽配置了 /api 代理，但当前请求未使用 /api 前缀，因此默认不生效，除非你显式改造为走代理。

## 2. 环境变量（关键）

项目已包含环境文件：
- .env.development（开发环境）
- .env.production（生产环境）

开发环境建议配置（与后端默认 8081 保持一致）：
```env
VITE_API_BASE_URL=http://localhost:8081
```

生产环境示例（按实际部署地址修改）：
```env
VITE_API_BASE_URL=https://your-prod-domain.com
```

注意：
- 修改 .env.* 后需要重启前端开发服务使其生效。
- 前端所有请求均通过 src/config/api.js 的 getApiUrl(endpoint) 基于 VITE_API_BASE_URL 生成完整地址。

## 3. 本地启动步骤（仅前端）

1) 安装依赖
```bash
npm install
```

2) 启动开发服务（vite.config.js 已固定端口为 8080）
```bash
npm run dev
```

- 启动成功后访问：http://localhost:8080
- 页面中的接口请求将指向 VITE_API_BASE_URL（默认 http://localhost:8081）

（可选）临时改端口（仅当你明确要改，且需同步调整 strictPort 或命令行参数）：
```bash
npm run dev -- --port 8080
```

## 4. 连接验证与常见问题（精简）

- 打开 http://localhost:8080，进行登录或发送消息等操作：
  - 如果 Network 请求能访问到 http://localhost:8081/jane/...，说明前后端联通正常。
  - 如果看到 CORS（跨域）报错：
    - 通常是后端未允许来自 http://localhost:8080 的跨域访问；
    - 前端仅需确认 VITE_API_BASE_URL 正确即可，CORS 需在后端放行。
  - 如果是网络错误（Failed to fetch）：
    - 检查后端是否已在 8081 端口运行；
    - 检查     是否与后端真实地址一致；
    - 检查本机防火墙/安全软件是否阻拦本地端口。
  - 如果 404：
    - 核对 API 路径是否与后端一致（见 src/config/api.js 的 ENDPOINTS）。

## 5. 端口修改指引

- 修改前端端口（不建议随意更改，默认 8080 最简单）：
  - 编辑 vite.config.js -> server.port 改为目标端口；若端口被占用，strictPort=true 会直接退出，改端口或释放占用即可。
- 修改后端端口（例如改为 9000）：
  - 同步修改 .env.development：
    ```env
    VITE_API_BASE_URL=http://localhost:9000
    ```
  - 重启前端开发服务。

## 6. 关于开发代理（仅在需要时）

- vite.config.js 中配置了 /api -> http://localhost:8081 的代理，但当前代码默认直连 BASE_URL。
- 只有当你将请求统一改为以 /api 开头（或在 getApiUrl 中拼接 /api 前缀）时，该代理才会生效。
- 默认推荐保留直连 BASE_URL 的实现，配置简单、语义清晰。

## 7. 部署成功验证清单

- [ ] 访问 http://localhost:8080 能正常打开页面
- [ ] 登录/注册/聊天可用，请求指向 http://localhost:8081/jane/...
- [ ] 无 CORS 报错
- [ ] 切换端口或后端地址后，已同步更新 .env.development 并重启前端