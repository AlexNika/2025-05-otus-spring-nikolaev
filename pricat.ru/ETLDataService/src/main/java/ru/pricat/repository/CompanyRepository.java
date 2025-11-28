package ru.pricat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;
import ru.pricat.model.entity.Company;

import java.util.Optional;

/**
 * Репозиторий для работы с компаниями, поставщиками прайс-листов
 */
@Repository
@RepositoryRestResource(path = "companies", collectionResourceRel = "companies")
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Метод находит компанию по companyFolder
     *
     * @param companyFolder наименование папки компании
     * @return Company класс или null
     */
    @RestResource(path = "companies", rel = "bycompanyfolder")
    Optional<Company> findByCompanyFolderIgnoreCase(String companyFolder);
}