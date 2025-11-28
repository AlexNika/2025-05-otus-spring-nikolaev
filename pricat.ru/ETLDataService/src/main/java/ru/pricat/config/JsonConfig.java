package ru.pricat.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Instant;

/**
 * Конфигурация для JSON парсинга.
 */
@Configuration
@SuppressWarnings("SpellCheckingInspection")
public class JsonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(Instant.class, new JsonDeserializer<>() {
            @Override
            public Instant deserialize(JsonParser p, DeserializationContext ctxt)
                    throws java.io.IOException {
                String value = p.getValueAsString();
                return parseLenientInstant(value);
            }

            private Instant parseLenientInstant(String value) {
                try {
                    return Instant.parse(value);
                } catch (Exception e) {
                    if (value.contains(".") && value.contains("Z")) {
                        int dotIndex = value.indexOf(".");
                        int zIndex = value.indexOf("Z");
                        if (zIndex > dotIndex + 4) {
                            value = value.substring(0, dotIndex + 4) + "Z";
                            return Instant.parse(value);
                        }
                    }
                    throw new RuntimeException("Failed to parse Instant: " + value, e);
                }
            }
        });
        mapper.registerModule(javaTimeModule);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    public JsonFactory jsonFactory(ObjectMapper objectMapper) {
        return objectMapper.getFactory();
    }
}
