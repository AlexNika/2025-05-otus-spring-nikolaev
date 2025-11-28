package ru.pricat.model.dto.company;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.pricat.model.entity.Company;

/**
 * DTO for {@link Company}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanyDto(String companyNameRU, String companyNameEN, Boolean isActive) {
}