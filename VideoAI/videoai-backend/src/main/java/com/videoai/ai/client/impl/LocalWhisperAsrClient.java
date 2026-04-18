package com.videoai.ai.client.impl;

import com.videoai.ai.client.AsrClient;
import com.videoai.ai.model.AsrResult;
import com.videoai.ai.model.AsrSegment;
import com.videoai.common.exception.BusinessException;
import com.videoai.common.util.JsonUtils;
import com.videoai.config.VideoAiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "videoai.asr", name = "mock", havingValue = "false")
public class LocalWhisperAsrClient implements AsrClient {

    private final VideoAiProperties properties;

    public LocalWhisperAsrClient(VideoAiProperties properties) {
        this.properties = properties;
    }

    @Override
    public AsrResult transcribe(File audioFile, String taskNo) {
        VideoAiProperties.Asr asr = properties.getAsr();
        if (asr.getScriptPath() == null || asr.getScriptPath().isBlank()) {
            throw new BusinessException("ASR script path is not configured");
        }

        File outputDir = new File("data/temp/asr");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new BusinessException("Unable to create ASR output directory");
        }
        File outputFile = new File(outputDir, taskNo + ".json");

        List<String> command = new ArrayList<>();
        command.add(asr.getPythonCommand());
        command.add(asr.getScriptPath());
        command.add("--input");
        command.add(audioFile.getAbsolutePath());
        command.add("--output");
        command.add(outputFile.getAbsolutePath());
        command.add("--model");
        command.add(asr.getModelName());
        command.add("--language");
        command.add(asr.getLanguage());

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new BusinessException("ASR transcription failed: " + output);
            }
            if (!outputFile.exists()) {
                throw new BusinessException("ASR output file was not generated");
            }

            String json = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
            Map<String, Object> payload = JsonUtils.toMap(json);
            return AsrResult.builder()
                    .language(String.valueOf(payload.getOrDefault("language", asr.getLanguage())))
                    .durationMs(toLong(payload.get("durationMs")))
                    .transcriptText(String.valueOf(payload.getOrDefault("transcriptText", "")))
                    .segments(parseSegments(payload.get("segments")))
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("ASR transcription failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<AsrSegment> parseSegments(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<AsrSegment> segments = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> rawMap)) {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) rawMap;
            segments.add(AsrSegment.builder()
                    .index(toInt(map.get("index")))
                    .startTimeMs(toLong(map.get("startTimeMs")))
                    .endTimeMs(toLong(map.get("endTimeMs")))
                    .content(String.valueOf(map.getOrDefault("content", "")))
                    .build());
        }
        return segments;
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return Math.round(Double.parseDouble(String.valueOf(value)));
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        return (int) Math.round(Double.parseDouble(String.valueOf(value)));
    }
}
