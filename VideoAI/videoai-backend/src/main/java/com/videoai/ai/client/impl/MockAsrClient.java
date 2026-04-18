package com.videoai.ai.client.impl;

import cn.hutool.core.io.FileUtil;
import com.videoai.ai.client.AsrClient;
import com.videoai.ai.model.AsrResult;
import com.videoai.ai.model.AsrSegment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class MockAsrClient implements AsrClient {

    @Override
    public AsrResult transcribe(File audioFile, String taskNo) {
        String baseName = FileUtil.mainName(audioFile.getName());
        List<String> sentences = List.of(
                "本视频围绕 " + baseName + " 的核心主题展开，介绍了背景、目标与关键问题。",
                "随后讲解了实现路径，包括上传链路、异步任务编排、语音转写与 AI 摘要。",
                "视频中进一步强调了 Agent 工具调用、RAG 检索增强以及成本治理的重要性。",
                "最后总结了系统的工程亮点、高并发治理方案以及适合校招展示的讲解重点。"
        );
        List<AsrSegment> segments = new ArrayList<>();
        StringBuilder fullText = new StringBuilder();
        for (int i = 0; i < sentences.size(); i++) {
            fullText.append(sentences.get(i)).append("\n");
            segments.add(AsrSegment.builder()
                    .index(i)
                    .startTimeMs(i * 15_000L)
                    .endTimeMs((i + 1) * 15_000L)
                    .content(sentences.get(i))
                    .build());
        }
        return AsrResult.builder()
                .language("zh-CN")
                .durationMs(60_000L)
                .transcriptText(fullText.toString())
                .segments(segments)
                .build();
    }
}
