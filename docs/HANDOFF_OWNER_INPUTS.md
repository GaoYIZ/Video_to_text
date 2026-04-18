# Owner Inputs Checklist

This document contains every personal or environment-specific item to confirm after code delivery.

## 1) Database
- MySQL host: `localhost`
- MySQL port: `3306`
- MySQL database: `video_db`
- MySQL username: `root`
- MySQL password: `gao200039` (already wired for local dev; move to env before production)

## 2) Redis
- Redis host: `localhost`
- Redis port: `6379`
- Redis password: leave empty unless your local Redis is protected

## 3) Agent Model Credentials
- `OPENAI_API_KEY` in `video-agent/.env`
- Optional custom endpoint: `OPENAI_BASE_URL`
- Optional model id: `LLM_MODEL` (default: `qwen-plus`)

If keys are missing, the app still runs in fallback demo mode (structured but lower-quality output).

## 4) Runtime Tooling on Host
- `ffmpeg` available in PATH
- Optional: `yt-dlp` available in PATH for non-direct URL parsing

If `yt-dlp` is missing, URL jobs only support direct media links.

## 5) Security Hardening Before Production
- Move DB/Redis/Agent secrets from `application.yml` and `.env` into environment variables
- Restrict CORS to your frontend domain
- Set upload size limits and cleanup policy according to your ECS disk budget