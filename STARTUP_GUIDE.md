# 🚀 项目启动指南

## 前置要求

确保已安装以下软件:
- ✅ Java 17+
- ✅ Maven 3.6+
- ✅ Python 3.10+
- ✅ Node.js 18+
- ✅ MySQL 8.0
- ✅ FFmpeg

---

## 第一步: 数据库初始化

1. 启动 MySQL 服务
2. 执行 SQL 脚本:
```bash
mysql -u root -p < video-backend/src/main/resources/db/init.sql
```

3. 修改数据库配置 (如需要):
   - 文件: `video-backend/src/main/resources/application.yml`
   - 修改 `spring.datasource.username` 和 `password`

---

## 第二步: 启动 Python AI 服务

```bash
cd video-agent

# 创建虚拟环境
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate
# Mac/Linux:
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt

# 配置环境变量
copy .env.example .env  # Windows
# cp .env.example .env  # Mac/Linux

# 编辑 .env 文件,填入你的 OpenAI API Key
# OPENAI_API_KEY=sk-your-api-key-here

# 启动服务
python main.py
```

服务将运行在: http://localhost:8000

---

## 第三步: 启动 Java 后端

```bash
cd video-backend

# 编译并启动
mvn spring-boot:run
```

服务将运行在: http://localhost:8080

---

## 第四步: 启动 Vue 前端

```bash
cd video-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将运行在: http://localhost:3000

---

## 使用流程

1. 打开浏览器访问: http://localhost:3000
2. 上传视频文件 (支持拖拽)
3. 点击"开始处理"按钮
4. 等待自动完成:
   - 上传视频 → 转换音频 → AI 分析
5. 查看总结报告

---

## API 接口说明

### 后端接口 (Java - 8080)
- `POST /api/video/upload` - 上传视频
- `POST /api/video/convert/{id}` - 转换音频
- `POST /api/video/process/{id}` - AI 处理
- `GET /api/video/{id}` - 获取视频信息

### AI 接口 (Python - 8000)
- `POST /api/process` - 转录+总结
- `POST /api/transcribe` - 仅转录
- `POST /api/summarize` - 仅总结

---

## 常见问题

### 1. FFmpeg 未找到
- Windows: 下载 FFmpeg 并添加到系统 PATH
- Mac: `brew install ffmpeg`
- Linux: `sudo apt-get install ffmpeg`

### 2. OpenAI API Key 错误
- 确保在 `.env` 文件中正确配置了 `OPENAI_API_KEY`
- 检查 API Key 是否有效且有余额

### 3. 端口冲突
- 修改对应服务的配置文件中的端口号
- Java: `video-backend/src/main/resources/application.yml`
- Python: `video-agent/.env`
- Vue: `video-frontend/vite.config.js`

### 4. 跨域问题
- 后端已配置 CORS,允许所有来源
- 前端通过 Vite 代理转发请求到后端

---

## 技术栈

- **后端**: Spring Boot 3.2 + MyBatis Plus + MySQL
- **AI**: Python FastAPI + Whisper + LangChain + OpenAI
- **前端**: Vue 3 + Vite + Element Plus + Axios
- **工具**: FFmpeg (音视频转换)

---

## 注意事项

⚠️ **不要提交到 Git 的文件**:
- `uploads/` 目录 (视频和音频文件)
- `venv/` 目录 (Python 虚拟环境)
- `node_modules/` 目录 (Node 依赖)
- `.env` 文件 (包含敏感信息)
- `target/` 和 `dist/` 目录 (构建输出)

✅ **可以安全提交**:
- 源代码文件
- 配置文件模板 (.env.example)
- 依赖声明文件 (pom.xml, requirements.txt, package.json)
