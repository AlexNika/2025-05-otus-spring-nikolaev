package ru.pricat.model.dto.company.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.pricat.model.dto.company.CompanyDto;
import ru.pricat.model.entity.Company;

@SuppressWarnings("unused")
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface CompanyMapper {
    Company toEntity(CompanyDto companyDto);

    CompanyDto toCompanyDto(Company company);

    Company updateWithNull(CompanyDto companyDto, @MappingTarget Company company);
}