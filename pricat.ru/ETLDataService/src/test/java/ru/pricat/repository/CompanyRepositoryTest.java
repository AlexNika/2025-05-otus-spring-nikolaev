package ru.pricat.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.pricat.model.entity.Company;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=etldataprocessor"
})
public class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Company company;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setCompanyNameRU("Тестовая компания");
        company.setCompanyNameEN("Test Company");
        company.setCompanyFolder("testCompanyFolder");
        company.setIsActive(true);
    }

    @Test
    void shouldFindCompanyByCompanyFolderIgnoreCase() {
        testEntityManager.persistAndFlush(company);

        Optional<Company> found = companyRepository.findByCompanyFolderIgnoreCase("TESTCOMPANYFOLDER");

        assertThat(found).isPresent();
        assertThat(found.get().getCompanyNameEN()).isEqualTo("Test Company");
    }

    @Test
    void shouldNotFindCompany_WhenCompanyFolderDoesNotExist() {
        Optional<Company> found = companyRepository.findByCompanyFolderIgnoreCase("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindCompany_WhenMixedCaseProvided() {
        testEntityManager.persistAndFlush(company);

        Optional<Company> found = companyRepository.findByCompanyFolderIgnoreCase("TeStCoMpAnYfOlDeR");

        assertThat(found).isPresent();
        assertThat(found.get().getCompanyFolder()).isEqualTo("testCompanyFolder");
    }
}
