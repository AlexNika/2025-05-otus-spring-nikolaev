package ru.pricat.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.pricat.model.BaseEntity;

import static lombok.AccessLevel.PRIVATE;

/**
 * Сущность для хранения параметров компаний поставщиков прайс-листов
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Table(name = "companies", schema = "etldataprocessor")
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "company_name_ru", nullable = false)
    String companyNameRU;

    @Column(name = "company_name_en", nullable = false)
    String companyNameEN;

    @Column(name = "company_folder", nullable = false)
    String companyFolder;

    @Column(name = "is_active", nullable = false)
    Boolean isActive;
}
