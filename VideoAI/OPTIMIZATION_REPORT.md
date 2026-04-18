# VideoAI 优化报告

## 本轮优化目标

本次优化聚焦 4 个方向：

1. 将摘要能力升级为真正的 `SummaryAgent`
2. 强化 `TaskAssistantAgent` 的失败诊断和耗时分析能力
3. 将 RAG 升级为关键词 + 语义的混合检索
4. 补齐 AI 成本治理，包括配额、统计和费用估算

## 已完成内容

### 1. SummaryAgent

- 新增 [SummaryAgent.java](/E:/Video/VideoAI/videoai-backend/src/main/java/com/videoai/agent/SummaryAgent.java)
- 新增 [summary-agent-system.txt](/E:/Video/VideoAI/videoai-backend/src/main/resources/prompts/summary-agent-system.txt)
- `SummaryServiceImpl` 支持优先走 LangChain4j Agent，Agent 不可用时自动降级到原有客户端调用
- 输出仍然保持结构化字段：
  - `title`
  - `summary`
  - `outline`
  - `keywords`
  - `highlights`
  - `qaSuggestions`

### 2. TaskAssistantAgent 能力增强

- 扩展 [VideoAgentTools.java](/E:/Video/VideoAI/videoai-backend/src/main/java/com/videoai/agent/VideoAgentTools.java)
- 新增工具：
  - `getTaskFailureReason`
  - `getTaskDurationStats`
- Agent 可回答：
  - 任务现在到哪一步了
  - 为什么失败
  - 哪个环节最慢
  - 是否适合重试

### 3. 混合检索 RAG

- 新增 [VectorSearchService.java](/E:/Video/VideoAI/videoai-backend/src/main/java/com/videoai/service/VectorSearchService.java)
- 新增 [VectorSearchServiceImpl.java](/E:/Video/VideoAI/videoai-backend/src/main/java/com/videoai/service/impl/VectorSearchServiceImpl.java)
- 当前检索策略：
  - 关键词检索
  - 基于词频和余弦相似度的简化语义检索
  - 使用 `RRF` 融合两种结果
- `RagServiceImpl` 默认把 `retrieve()` 升级为混合检索

### 4. AI 成本治理

- `user` 表新增：
  - `daily_quota_limit`
  - `monthly_quota_limit`
- 新增返回模型：
  - [UserQuotaVO.java](/E:/Video/VideoAI/videoai-backend/src/main/java/com/videoai/model/vo/UserQuotaVO.java)
  - [UsageStatsVO.java](/E:/Video/VideoAI/videoai-backend/src/main/java/com/videoai/model/vo/UsageStatsVO.java)
- `UsageService` 支持：
  - 配额检查
  - 今日统计
  - 本月统计
  - 按业务类型和模型维度聚合
  - Token 成本估算

### 5. 前端展示增强

- 重写 [UsageView.vue](/E:/Video/VideoAI/videoai-frontend/src/views/UsageView.vue)
- 页面新增：
  - 配额进度条
  - 今日 / 本月切换
  - Token 和费用估算展示
  - 业务类型分布
  - 模型分布

## 审查结论

这轮优化的方向是对的，尤其是 Agent、RAG 和成本治理三个方向，确实把项目从“普通 CRUD + 模型调用”往“更像工程化 AI 项目”推了一步。

但在我接手审查时，代码还存在几类问题：

- 后端并未通过编译，存在模型字段不匹配、方法名错误和数值类型处理错误
- 多个 Prompt、页面和报告文件出现乱码，交付质量不足
- 混合检索实现里存在结果融合后只返回关键词结果子集的问题
- SQL 尚未把新配额字段真正落到建表脚本

这些问题已经在本次修复中一起处理。

## 本次补充修复

- 修复 `VideoAgentTools` 中的编译错误
- 修复 `SummaryServiceImpl` 中 `SummaryResult` 构建和 token 估算逻辑
- 修复 `UsageServiceImpl` 中统计和平均耗时逻辑
- 修复混合检索结果融合逻辑
- 修复 `UsageView.vue` 和 Prompt 文件乱码
- 更新 SQL 建表脚本和默认演示用户配额
- 补充本地无 Docker 启动脚本

## 当前状态

已验证：

- 后端 `mvn -q -DskipTests compile` 通过
- 前端 `npm run build` 通过

## 面试可讲亮点

- 使用 LangChain4j 构建多 Agent 协作模式，支持结构化摘要、任务状态解释和工具增强问答
- 设计混合检索 RAG，将关键词召回与语义召回结合，并用 RRF 做结果融合
- 引入 AI 成本治理体系，落地每日 / 每月配额、统计面板、费用估算和缓存治理
- 通过任务事件日志与状态机设计提升视频处理链路的可观测性
