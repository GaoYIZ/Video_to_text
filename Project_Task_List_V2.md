# 🎬 视频提取文字与 AI Agent 总结项目任务书 (V2)

## 1. 项目概述
本项目旨在构建一个高集成度的视频处理平台。用户上传视频后，后端通过 FFmpeg 提取音频，由 Python 驱动的 AI Agent（基于 LangChain 和 Whisper）进行语音转文字及多维度摘要生成。

## 2. 技术架构 (轻量化、无 Docker 设计)
* **后端**: Spring Boot 3.x (处理业务逻辑、本地文件管理)
* **AI 层**: Python 3.10+ (LangChain, FastAPI, Whisper API)
* **数据库**: 本地 MySQL 8.0
* **存储**: 本地文件系统 (`/uploads/videos`, `/uploads/audios`)
* **前端**: Vue 3 + Element Plus

---

## 3. 详细任务清单 (配合 Lingma Agent 逐步执行)

### 第一阶段：项目初始化与 Git 配置
- [ ] **任务 1.1**: 在本地创建项目根目录，初始化 Git 仓库：`git init`。
- [ ] **任务 1.2**: 设置远程仓库：`git remote add origin https://github.com/GaoYIZ/Video_to_text`。
- [ ] **任务 1.3**: 编写 `.gitignore` 文件（忽略 target、venv、.idea、uploads 文件夹以及敏感配置）。
- [ ] **任务 1.4**: 创建三个子文件夹：`video-backend`, `video-agent`, `video-frontend`。

### 第二阶段：后端逻辑开发 (Java)
- [ ] **任务 2.1**: **[Java]** 初始化 Spring Boot 项目，配置本地 MySQL 连接。
- [ ] **任务 2.2**: **[Java]** 编写本地上传接口，将视频流保存至本地指定目录。
- [ ] **任务 2.3**: **[Java]** 集成 FFmpeg 命令行调用，实现视频转 MP3 功能。

### 第三阶段：AI 智能体开发 (Python)
- [ ] **任务 3.1**: **[Python]** 创建虚拟环境 `venv`，安装 `fastapi`, `langchain`, `openai` 等依赖。
- [ ] **任务 3.2**: **[Python]** 编写 LangChain 总结逻辑，支持对转录文本进行要点提炼。
- [ ] **任务 3.3**: **[Python]** 实现 FastAPI 接口，供 Java 后端发送文本并接收总结结果。

### 第四阶段：前端开发与 Git 提交
- [ ] **任务 4.1**: **[Vue]** 搭建基础页面，包含文件上传进度条和总结展示区域。
- [ ] **任务 4.2**: **[Git]** 完成第一个可用版本后，执行：
    * `git add .`
    * `git commit -m "feat: 初步完成视频上传与文字提炼框架"`
    * `git push -u origin main` (或 master)

---

## 4. 给 Lingma Agent 的专项指令

### 关于 Git 提交
> "请帮我写一个标准的 `.gitignore` 文件，要求能同时适用于 Java、Python 和 Vue 项目，并且不要把本地上传的视频文件上传到 GitHub。"

### 关于代码编写
> "请帮我写一个 Java 类，能异步调用本地的 Python 接口，并等待返回总结结果。"

---

## 5. 注意事项 (针对硬件性能优化)
1. **本地运行**: 请确保你的电脑已安装 MySQL 客户端和 FFmpeg 工具。
2. **轻量提交**: 不要将任何视频素材、音频素材或 Python 虚拟环境目录提交到 Git 仓库，保持仓库精简。
3. **API 密匙**: 如果使用 OpenAI 等 API，请将 Key 放在环境变量中，切勿硬编码提交到 GitHub。
