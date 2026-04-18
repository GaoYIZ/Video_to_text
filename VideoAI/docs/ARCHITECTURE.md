# VideoAI 架构说明

## 后端分层

`videoai-backend` 采用单体分层结构，核心包如下：

- `controller`：REST 接口层
- `service / service.impl`：业务逻辑层
- `mapper`：MyBatis-Plus 数据访问层
- `model.entity / dto / vo / enums`：领域模型
- `config`：JWT、MyBatis、Redis、Redisson、LangChain4j、OpenAPI 配置
- `mq`：RocketMQ 消息投递与消费
- `storage`：MinIO / Local 存储抽象
- `ai`：ASR / DeepSeek 客户端与模型
- `agent`：VideoQAAgent、TaskAssistantAgent、Tools
- `common`：统一响应、异常、工具类、上下文

## 前端分层

`videoai-frontend` 采用 Vue3 + Vite + TS：

- `views`：页面级视图
- `components`：布局与通用组件
- `api`：Axios 请求封装
- `stores`：Pinia 状态管理
- `router`：路由守卫与页面组织
- `types`：类型定义
- `styles`：全局主题样式

## 状态机设计

任务状态枚举：

- `PENDING`
- `UPLOADED`
- `QUEUED`
- `PROCESSING_AUDIO`
- `TRANSCRIBING`
- `SUMMARIZING`
- `INDEXING`
- `SUCCESS`
- `FAILED`

每次状态切换都会写入 `task_event_log`，便于：

- 前端展示时间线
- 失败原因排查
- 节点耗时统计

## Redis Key 设计

- `upload:chunk:{uploadId}`
- `upload:progress:{fileMd5}`
- `lock:video:task:{taskId}`
- `lock:video:file:{md5}`
- `rate_limit:user:{userId}`
- `chat:memory:{userId}:{taskId}:{sessionId}`
- `ai:summary:cache:{fileId}`
- `ai:qa:cache:{taskId}:{questionHash}`
- `mq:consume:idempotent:{messageKey}`

## RocketMQ 设计

Topic：

- `video-task-topic`
- `video-retry-topic`（当前预留）

消息体：

- `taskId`
- `taskNo`
- `fileId`
- `fileMd5`
- `userId`
- `step`
- `timestamp`

消费保障：

- Redis 幂等键防止重复消费
- 失败节点通过任务日志和手动重试接口恢复

