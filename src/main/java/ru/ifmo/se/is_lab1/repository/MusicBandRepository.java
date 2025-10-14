package ru.ifmo.se.is_lab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ifmo.se.is_lab1.domain.Mood;
import ru.ifmo.se.is_lab1.domain.MusicBand;

import java.math.BigDecimal;
import java.util.List;

public interface MusicBandRepository extends JpaRepository<MusicBand, Long>, JpaSpecificationExecutor<MusicBand> {

    @Query("select coalesce(sum(b.impactSpeed), 0) from MusicBand b")
    BigDecimal sumImpactSpeed();

    List<MusicBand> findBySoundtrackNameStartingWithIgnoreCase(String prefix);

    @Modifying(clearAutomatically = true)
    @Query("update MusicBand b set b.mood = :target where b.mood = :source")
    int bulkUpdateMood(@Param("source") Mood source, @Param("target") Mood target);
}
