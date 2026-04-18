package com.videoai.ai.client.impl;

import com.videoai.ai.client.DeepSeekClient;
import com.videoai.ai.model.QuestionAnswerResult;
import com.videoai.ai.model.SummaryResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "videoai.ai", name = "mock", havingValue = "true", matchIfMissing = true)
public class MockDeepSeekClient implements DeepSeekClient {

    @Override
    public SummaryResult summarize(String transcriptText) {
        List<String> lines = transcriptText.lines().filter(line -> !line.isBlank()).toList();
        return SummaryResult.builder()
                .title("VideoAI 视频内容智能摘要")
                .summary(lines.isEmpty() ? "暂无转写内容" : lines.get(0))
                .outline(List.of("背景介绍", "实现链路", "Agent 与 RAG 能力", "工程亮点总结"))
                .keywords(List.of("视频解析", "RocketMQ", "LangChain4j", "RAG", "成本治理"))
                .highlights(List.of("异步任务编排", "分布式锁防重", "视频理解智能体", "缓存与配额治理"))
                .qaSuggestions(List.of("这个视频主要讲了什么？", "系统架构有什么亮点？", "为什么需要 RAG？"))
                .modelName("mock-deepseek")
                .promptTokens(Math.max(transcriptText.length() / 4, 100))
                .completionTokens(180)
                .totalTokens(Math.max(transcriptText.length() / 4, 100) + 180)
                .durationMs(320L)
                .build();
    }

    @Override
    public QuestionAnswerResult answer(String systemPrompt, String userPrompt, List<String> contextSegments) {
        String context = contextSegments.isEmpty() ? "暂无命中片段" : String.join("；", contextSegments);
        String answer = """
                基于视频内容，我优先命中了这些片段：%s。
                针对你的问题“%s”，可以得出结论：视频重点介绍了从上传、异步处理、转写、总结到 Agent 问答的完整链路，并通过 RAG 检索和缓存治理提升回答精度与成本控制。
                """.formatted(context, userPrompt);
        return QuestionAnswerResult.builder()
                .answer(answer)
                .modelName("mock-deepseek")
                .promptTokens(220)
                .completionTokens(180)
                .totalTokens(400)
                .durationMs(260L)
                .cacheHit(false)
                .build();
    }
}
