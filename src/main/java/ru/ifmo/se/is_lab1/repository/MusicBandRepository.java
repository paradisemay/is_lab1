package ru.ifmo.se.is_lab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.domain.Mood;
import ru.ifmo.se.is_lab1.domain.MusicBand;

import java.math.BigDecimal;
import java.util.List;

public interface MusicBandRepository extends JpaRepository<MusicBand, Long>, JpaSpecificationExecutor<MusicBand> {

    @Query("select coalesce(sum(b.impactSpeed), 0) from MusicBand b")
    BigDecimal sumImpactSpeed();

    long countByImpactSpeedLessThan(BigDecimal threshold);

    List<MusicBand> findBySoundtrackNameStartingWithIgnoreCase(String prefix);

    List<MusicBand> findByCarIsNull();

    @Modifying(clearAutomatically = true)
    @Query("update MusicBand b set b.mood = :target where b.mood = :source")
    int bulkUpdateMood(@Param("source") Mood source, @Param("target") Mood target);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update MusicBand b set b.mood = :mood")
    int updateMoodForAll(@Param("mood") Mood mood);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update MusicBand b set b.car = :car where b.car is null")
    int assignCarToAllWithoutCar(@Param("car") Car car);
}
