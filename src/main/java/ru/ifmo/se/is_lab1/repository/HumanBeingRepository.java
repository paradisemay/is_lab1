package ru.ifmo.se.is_lab1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.ifmo.se.is_lab1.model.HumanBeing;
import ru.ifmo.se.is_lab1.model.Mood;

public interface HumanBeingRepository extends JpaRepository<HumanBeing, Long>, JpaSpecificationExecutor<HumanBeing> {

    @Query("select coalesce(sum(h.impactSpeed), 0.0) from HumanBeing h")
    Double sumImpactSpeed();

    List<HumanBeing> findBySoundtrackNameStartingWithIgnoreCase(String prefix);

    @Modifying(clearAutomatically = true)
    @Query("update HumanBeing h set h.mood = :target where h.mood = :source")
    int bulkUpdateMood(@Param("source") Mood source, @Param("target") Mood target);
}
