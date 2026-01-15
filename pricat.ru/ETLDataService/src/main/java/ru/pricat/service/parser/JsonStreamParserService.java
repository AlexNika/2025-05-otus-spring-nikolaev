package ru.pricat.service.parser;

import ru.pricat.model.dto.events.JsonFileHeader;
import ru.pricat.model.dto.events.PriceFileWrapper;
import ru.pricat.model.dto.events.PriceItemDto;

import java.io.InputStream;
import java.util.function.Consumer;

public interface JsonStreamParserService {
    PriceFileWrapper parseFullFile(InputStream inputStream);

    void parseStream(InputStream inputStream,
                     Consumer<PriceItemDto> itemConsumer,
                     Consumer<String> errorConsumer);

    boolean validateJsonStructure(InputStream inputStream);

    int countItems(InputStream inputStream);

    JsonFileHeader extractMetadata(InputStream inputStream);
}
