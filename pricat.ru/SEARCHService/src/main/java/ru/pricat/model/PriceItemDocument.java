package ru.pricat.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Document(indexName = "price-items")
public class PriceItemDocument {

    @Id
    @Field(type = FieldType.Text, analyzer = "text_ngram_analyzer")
    private String id;

    @Field(type = FieldType.Text, analyzer = "text_ngram_analyzer")
    private String productName;

    @Field(type = FieldType.Text, analyzer = "text_ngram_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private String companyId;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Keyword)
    private String currency;

    @Field(type = FieldType.Integer)
    private Integer stockQuantity;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String manufacturer;

    @Field(type = FieldType.Keyword)
    private String supplierCode;

    @Field(type = FieldType.Date)
    private Instant fileProcessedAt;
}
