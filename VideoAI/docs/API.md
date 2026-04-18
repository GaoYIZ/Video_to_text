# VideoAI 接口文档

统一返回格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

## 用户接口

- `POST /api/user/register`
- `POST /api/user/login`
- `GET /api/user/me`
- `POST /api/user/logout`

## 上传接口

- `POST /api/upload/init`
- `GET /api/upload/chunks?uploadId=xxx`
- `POST /api/upload/chunk`
- `POST /api/upload/merge`

## 任务接口

- `POST /api/video-task/create`
- `GET /api/video-task/page`
- `GET /api/video-task/{id}`
- `GET /api/video-task/{id}/status`
- `POST /api/video-task/{id}/retry`
- `GET /api/video-task/{id}/events`
- `DELETE /api/video-task/{id}`

## 结果接口

- `GET /api/video-result/{taskId}/transcript`
- `GET /api/video-result/{taskId}/summary`
- `GET /api/video-result/{taskId}/segments`

## AI / Agent 接口

- `POST /api/chat/ask`
- `GET /api/chat/history`
- `POST /api/agent/video/ask`
- `GET /api/agent/video/tools/debug`
- `GET /api/usage/me`

## 典型请求示例

### 1. 登录

```http
POST /api/user/login
Content-Type: application/json

{
  "username": "demo_user",
  "password": "123456"
}
```

### 2. 初始化上传

```http
POST /api/upload/init
Authorization: Bearer xxx
Content-Type: application/json

{
  "fileName": "demo.mp4",
  "fileMd5": "5d41402abc4b2a76b9719d911017c592",
  "fileSize": 10485760,
  "chunkSize": 5242880,
  "totalChunks": 2,
  "mimeType": "video/mp4"
}
```

### 3. 创建任务

```http
POST /api/video-task/create
Authorization: Bearer xxx
Content-Type: application/json

{
  "uploadId": "UP202604170001"
}
```

### 4. Agent 问答

```http
POST /api/agent/video/ask
Authorization: Bearer xxx
Content-Type: application/json

{
  "taskId": 1,
  "question": "这个视频的核心工程亮点是什么？",
  "sessionId": "agent-1"
}
```

