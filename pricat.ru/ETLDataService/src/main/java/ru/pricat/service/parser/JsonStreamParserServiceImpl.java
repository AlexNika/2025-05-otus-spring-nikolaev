package ru.pricat.service.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pricat.exception.JsonParserException;
import ru.pricat.model.dto.events.JsonFileHeader;
import ru.pricat.model.dto.events.PriceFileWrapper;
import ru.pricat.model.dto.events.PriceItemDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Сервис для потокового парсинга JSON файлов с прайс-листами.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("SpellCheckingInspection")
public class JsonStreamParserServiceImpl implements JsonStreamParserService {

    private final ObjectMapper objectMapper;

    private final JsonFactory jsonFactory;

    /**
     * Парсит весь JSON файл в память (для небольших файлов).
     */
    @Override
    public PriceFileWrapper parseFullFile(InputStream inputStream) {
        try {
            PriceFileWrapper wrapper = objectMapper.readValue(inputStream, PriceFileWrapper.class);
            log.debug("Successfully parsed JSON file with {} items",
                    wrapper.items() != null ? wrapper.items().size() : 0);
            return wrapper;
        } catch (IOException e) {
            log.error("Failed to parse JSON file: {}", e.getMessage());
            throw new JsonParserException("Failed to parse JSON file", e);
        }
    }

    /**
     * Потоковый парсинг больших JSON файлов.
     */
    @Override
    public void parseStream(InputStream inputStream,
                            Consumer<PriceItemDto> itemConsumer,
                            Consumer<String> errorConsumer) {
        try (JsonParser parser = jsonFactory.createParser(inputStream)) {
            JsonToken currentToken = parser.nextToken();
            while (currentToken != null) {
                if (currentToken == JsonToken.FIELD_NAME) {
                    String fieldName = parser.currentName();
                    if ("metadata".equals(fieldName)) {
                        parser.nextToken();
                        parser.skipChildren();
                    } else if ("items".equals(fieldName) && parser.nextToken() == JsonToken.START_ARRAY) {
                        processItemsArray(parser, itemConsumer, errorConsumer);
                    }
                }
                currentToken = parser.nextToken();
            }
        } catch (IOException e) {
            log.error("Streaming JSON parsing failed: {}", e.getMessage());
            throw new JsonParserException("Streaming JSON parsing failed", e);
        }
    }

    /**
     * Обрабатывает массив items в потоковом режиме.
     */
    private void processItemsArray(JsonParser parser,
                                   Consumer<PriceItemDto> itemConsumer,
                                   Consumer<String> errorConsumer) throws IOException {
        int processedCount = 0;
        int errorCount = 0;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            try {
                PriceItemDto item = objectMapper.readValue(parser, PriceItemDto.class);

                if (item.isValid()) {
                    itemConsumer.accept(item);
                    processedCount++;
                } else {
                    errorConsumer.accept("Invalid item: " + item.getProductId());
                    errorCount++;
                }

            } catch (IOException e) {
                errorConsumer.accept("Failed to parse item: " + e.getMessage());
                errorCount++;
            }
        }
        log.debug("Stream processing completed: {} items processed, {} errors",
                processedCount, errorCount);
    }

    /**
     * Валидирует структуру JSON файла без полного парсинга.
     */
    @Override
    public boolean validateJsonStructure(InputStream inputStream) {
        try (JsonParser parser = jsonFactory.createParser(inputStream)) {
            boolean hasMetadata = false;
            boolean hasItems = false;
            JsonToken currentToken = parser.nextToken();
            while (currentToken != null) {
                if (currentToken == JsonToken.FIELD_NAME) {
                    String fieldName = parser.currentName();
                    if ("metadata".equals(fieldName)) {
                        hasMetadata = true;
                    } else if ("items".equals(fieldName)) {
                        hasItems = true;
                    }
                }
                currentToken = parser.nextToken();
            }
            return hasMetadata && hasItems;
        } catch (IOException e) {
            log.warn("JSON structure validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Быстрое получение количества items в файле.
     */
    @Override
    public int countItems(InputStream inputStream) {
        try (JsonParser parser = jsonFactory.createParser(inputStream)) {
            int count = 0;
            JsonToken currentToken = parser.nextToken();
            while (currentToken != null) {
                if (currentToken == JsonToken.FIELD_NAME && "items".equals(parser.currentName())) {
                    if (parser.nextToken() == JsonToken.START_ARRAY) {
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            count++;
                            parser.skipChildren();
                        }
                    }
                    break;
                }
                currentToken = parser.nextToken();
            }
            return count;
        } catch (IOException e) {
            log.error("Failed to count items in JSON file: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Извлекает только метаданные из JSON файла.
     */
    @Override
    public JsonFileHeader extractMetadata(InputStream inputStream) {
        try (JsonParser parser = jsonFactory.createParser(inputStream)) {
            JsonToken currentToken = parser.nextToken();
            while (currentToken != null) {
                if (currentToken == JsonToken.FIELD_NAME && "metadata".equals(parser.currentName())) {
                    parser.nextToken();
                    return objectMapper.readValue(parser, JsonFileHeader.class);
                }
                currentToken = parser.nextToken();
            }
            throw new JsonParserException("Metadata not found in JSON file");
        } catch (IOException e) {
            log.error("Failed to extract metadata from JSON file: {}", e.getMessage());
            throw new JsonParserException("Failed to extract metadata from JSON file", e);
        }
    }
}
