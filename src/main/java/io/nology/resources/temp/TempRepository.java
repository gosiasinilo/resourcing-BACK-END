package io.nology.resources.temp;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.nology.resources.temp.entity.Temp;

@Repository
public interface TempRepository extends JpaRepository<Temp, Long> {
    @Query("""
                SELECT t FROM Temp t
                WHERE t.id NOT IN (
                    SELECT j.temp.id FROM Job j
                    WHERE j.temp IS NOT NULL
                    AND j.startDate <= :endDate
                    AND j.endDate >= :startDate
                )
            """)
    List<Temp> getAvailableTemps(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
