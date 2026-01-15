package ru.pricat.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import ru.pricat.model.PriceItemDocument;
import ru.pricat.repository.PriceListCurrentStateRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса поиска по документам прайс-листа в Elasticsearch.
 * Обеспечивает поиск по нескольким полям и фильтрацию по компании.
 */
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    /**
     * Операции для взаимодействия с Elasticsearch.
     * Используется для выполнения поисковых запросов.
     */
    private final ElasticsearchOperations operations;

    /**
     * Репозиторий для получения данных о текущем состоянии прайс-листа.
     * Используется для получения списка уникальных компаний.
     */
    private final PriceListCurrentStateRepository stateRepository;

    /**
     * Выполняет поиск документов в Elasticsearch по заданному запросу и фильтру по компании.
     *
     * @param query    Текст поискового запроса. Если пустой или null, возвращается пустая страница.
     * @param company  Идентификатор компании для фильтрации. Может быть null.
     * @param pageable Объект, определяющий параметры пагинации (номер страницы, размер и т.д.).
     * @return Страница найденных документов {@link PriceItemDocument}.
     */
    @Override
    public Page<PriceItemDocument> search(String query, String company, Pageable pageable) {
        Query esQuery = buildBoolQuery(query, company);
        NativeQuery nativeQuery = NativeQuery
                .builder()
                .withQuery(esQuery)
                .withPageable(pageable)
                .build();
        SearchHits<PriceItemDocument> hits = operations.search(nativeQuery, PriceItemDocument.class);
        List<PriceItemDocument> results = hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        long totalHits = hits.getTotalHits();
        return new PageImpl<>(results, pageable, totalHits);
    }

    /**
     * Строит составной логический запрос (BoolQuery), который включает:
     * - Поиск по нескольким полям с использованием MultiMatch (если query не пуст)
     * - Дополнительный поиск по шаблону (wildcard) по полям productName и id (для более гибкого совпадения)
     * - Фильтрацию по companyId (если company не пуст)
     *
     * @param query   Текст поискового запроса
     * @param company Идентификатор компании для фильтрации
     * @return Собранный объект Query
     */
    private Query buildBoolQuery(String query, String company) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        if (query != null && !query.trim().isEmpty()) {
            MultiMatchQuery.Builder mmq = new MultiMatchQuery.Builder()
                    .query(query)
                    .fields("id", "productName", "description", "supplierCode");
            boolBuilder.should(new Query.Builder().multiMatch(mmq.build()).build());
            boolBuilder.should(wildcardQuery("productName", query));
            boolBuilder.should(wildcardQuery("id", query));
        }
        if (company != null && !company.trim().isEmpty()) {
            boolBuilder.filter(new Query.Builder()
                    .term(t -> t.field("companyId").value(company))
                    .build());
        }
        return boolBuilder.build()._toQuery();
    }

    /**
     * Возвращает список уникальных идентификаторов компаний из репозитория.
     *
     * @return Список уникальных значений companyId
     */
    @Override
    public List<String> getAllCompanies() {
        return stateRepository.findDistinctCompanies().stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    /**
     * Создаёт wildcard-запрос для поиска по шаблону.
     * Например, для запроса "test" будет создан шаблон "*test*".
     *
     * @param field Имя поля, по которому производится поиск
     * @param value Значение, которое будет использоваться в шаблоне
     * @return Собранный Query с wildcard-поиском
     */
    private Query wildcardQuery(String field, String value) {
        return new Query.Builder()
                .wildcard(w -> w
                        .field(field)
                        .value("*" + value.toLowerCase() + "*"))
                .build();
    }
}