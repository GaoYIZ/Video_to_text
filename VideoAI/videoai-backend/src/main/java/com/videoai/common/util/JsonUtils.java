package com.videoai.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtils() {
    }

    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("json serialize error", e);
        }
    }

    public static <T> T fromJson(String value, Class<T> clazz) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("json parse error", e);
        }
    }

    public static <T> List<T> toList(String value, Class<T> clazz) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(value,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            log.error("json list parse error", e);
            return Collections.emptyList();
        }
    }

    public static List<String> toStringList(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("json string list parse error", e);
            return Collections.emptyList();
        }
    }

    public static Map<String, Object> toMap(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("json map parse error", e);
            return Collections.emptyMap();
        }
    }
}
