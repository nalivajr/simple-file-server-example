package org.remdev.services.fileserver.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper converter = new ObjectMapper();

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return converter.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> cls) {
        if (json == null) {
            return null;
        }
        try {
            return converter.readValue(json, cls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
