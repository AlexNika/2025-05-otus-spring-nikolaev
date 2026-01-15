package ru.pricat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pricat.model.PriceListCurrentState;

import java.util.List;

/**
 * Репозиторий для работы с текущим состоянием прайс-листа
 */
@Repository
public interface PriceListCurrentStateRepository extends JpaRepository<PriceListCurrentState, Long> {

    List<PriceListCurrentState> findByCompany(String company);

    @Query("SELECT DISTINCT p.company FROM PriceListCurrentState p")
    List<String> findDistinctCompanies();

    @Modifying
    @Transactional
    @Query("DELETE FROM PriceListCurrentState p WHERE p.company = :company")
    void deleteByCompany(String company);
}
