package ru.ifmo.se.is_lab1.repository;

import java.util.List;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.domain.HumanBeing;
import ru.ifmo.se.is_lab1.model.Mood;

public interface HumanBeingRepository extends JpaRepository<HumanBeing, Long>, JpaSpecificationExecutor<HumanBeing> {

    @Query("select coalesce(sum(h.impactSpeed), 0) from HumanBeing h")
    Long sumImpactSpeed();

    boolean existsByName(String name);

    long countByImpactSpeedLessThan(int threshold);

    List<HumanBeing> findBySoundtrackNameStartingWithIgnoreCase(String prefix);

    List<HumanBeing> findByCarIsNull();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from HumanBeing h where h.car is null")
    List<HumanBeing> findByCarIsNullForUpdate();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from HumanBeing h where h.id = :id")
    Optional<HumanBeing> findByIdForUpdate(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("update HumanBeing h set h.mood = :target where h.mood = :source")
    int bulkUpdateMood(@Param("source") Mood source, @Param("target") Mood target);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update HumanBeing h set h.mood = :mood")
    int updateMoodForAll(@Param("mood") Mood mood);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update HumanBeing h set h.car = :car where h.car is null")
    int assignCarToAllWithoutCar(@Param("car") Car car);

    default boolean hasNameAndSoundtrackConflict(String name, String soundtrackName, Long excludeId) {
        if (name == null || soundtrackName == null) {
            return false;
        }
        String normalizedName = name.trim();
        String normalizedSoundtrack = soundtrackName.trim();
        Specification<HumanBeing> specification = Specification
                .where(HumanBeingSpecifications.hasNameAndSoundtrack(normalizedName, normalizedSoundtrack));
        if (excludeId != null) {
            specification = specification.and(HumanBeingSpecifications.excludeId(excludeId));
        }
        return count(specification) > 0;
    }

    default boolean hasRealHeroImpactSpeedConflict(int impactSpeed, Long excludeId) {
        Specification<HumanBeing> specification = Specification
                .where(HumanBeingSpecifications.isRealHeroWithImpactSpeed(impactSpeed));
        if (excludeId != null) {
            specification = specification.and(HumanBeingSpecifications.excludeId(excludeId));
        }
        return count(specification) > 0;
    }
}
