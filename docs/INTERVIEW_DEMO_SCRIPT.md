# Interview Demo Script (10-12 minutes)

## 1) 30-second opening
"This project is a single-ECS, async video transcript + Agent QA platform. The highlight is high-concurrency task admission and traceable agent answers."

## 2) Show high-concurrency architecture
- Frontend submits job -> backend returns `jobId` immediately.
- Backend pushes `jobId` into Redis queue.
- Worker pool consumes jobs in background.
- Job progress/events stream to frontend over SSE.

Key phrase:
"Request path and processing path are decoupled."

## 3) Live product flow
1. Create one URL/audio/video job in homepage.
2. Open result page and show real-time timeline (`QUEUED -> PROCESSING -> ... -> DONE`).
3. Show transcript + summary + key points + mindmap.

## 4) Agent learning value
- Ask a domain question in Agent panel.
- Show answer + citations.
- Show trace steps (`retrieve_chunks`, `generate_answer`) and duration.

Key phrase:
"This is not black-box chat; each answer is evidence-grounded and traceable."

## 5) Failure and recovery
- Show a failed job in history (or explain with screenshot).
- Click retry and show status back to queued.

## 6) Performance story
- Show benchmark table from `PERF_RESULTS_TEMPLATE.md`.
- Explain tradeoff: single machine limits model throughput, but async queue keeps API responsive under bursts.

## 7) Close with roadmap
- Next step: horizontal scale with external workers.
- Add object storage and vector DB for cross-video QA.