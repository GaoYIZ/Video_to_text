# Deploy Runbook (Single ECS)

## 1) Initialize DB schema
From repo root:

```bash
mysql -u root -p < video-backend/src/main/resources/db/init.sql
```

## 2) Start Agent service

```bash
cd video-agent
python -m venv venv
# Windows
venv\Scripts\activate
# macOS/Linux
# source venv/bin/activate

pip install -r requirements.txt
copy .env.example .env  # Windows
# edit .env and set OPENAI_API_KEY / OPENAI_BASE_URL / LLM_MODEL if needed
python main.py
```

Agent default URL: `http://localhost:8000`

## 3) Start Backend

```bash
cd video-backend
mvn spring-boot:run
```

Backend default URL: `http://localhost:8080`

## 4) Start Frontend

```bash
cd video-frontend
npm install
npm run dev
```

Frontend default URL: `http://localhost:3000`

## 5) Smoke test
1. Open `/` and create a video/audio/url job.
2. Verify immediate `jobId` return and live timeline updates.
3. Open job detail page and confirm transcript/summary/mindmap output.
4. Ask a question in Agent panel and verify citation output.
5. Open history page and verify search/filter/delete/retry.