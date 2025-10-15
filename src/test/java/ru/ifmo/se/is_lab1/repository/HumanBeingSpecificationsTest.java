package ru.ifmo.se.is_lab1.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.is_lab1.domain.HumanBeing;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HumanBeingSpecificationsTest {

    @Mock
    private Root<HumanBeing> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder builder;

    @Test
    void withFilterShouldProducePredicatesForEveryField() {
        HumanBeingFilter filter = new HumanBeingFilter();
        filter.setName("Ivan");
        filter.setMood(Mood.SADNESS);
        filter.setWeaponType(WeaponType.SHOTGUN);
        filter.setMinImpactSpeed(10);
        filter.setMaxImpactSpeed(20);
        filter.setSoundtrackPrefix("Song");
        filter.setCarId(3L);
        filter.setRealHero(true);
        filter.setHasToothpick(false);

        Path<String> namePath = mock(Path.class);
        Path<Mood> moodPath = mock(Path.class);
        Path<WeaponType> weaponTypePath = mock(Path.class);
        Path<Integer> impactSpeedPath = mock(Path.class);
        Path<String> soundtrackPath = mock(Path.class);
        Path<Object> carPath = mock(Path.class);
        Path<Long> carIdPath = mock(Path.class);
        Path<Boolean> realHeroPath = mock(Path.class);
        Path<Boolean> hasToothpickPath = mock(Path.class);

        when(root.<String>get("name")).thenReturn(namePath);
        when(root.<Mood>get("mood")).thenReturn(moodPath);
        when(root.<WeaponType>get("weaponType")).thenReturn(weaponTypePath);
        when(root.<Integer>get("impactSpeed")).thenReturn(impactSpeedPath);
        when(root.<String>get("soundtrackName")).thenReturn(soundtrackPath);
        when(root.get("car")).thenReturn(carPath);
        when(carPath.<Long>get("id")).thenReturn(carIdPath);
        when(root.<Boolean>get("realHero")).thenReturn(realHeroPath);
        when(root.<Boolean>get("hasToothpick")).thenReturn(hasToothpickPath);

        Predicate namePredicate = mock(Predicate.class);
        Predicate moodPredicate = mock(Predicate.class);
        Predicate minPredicate = mock(Predicate.class);
        Predicate maxPredicate = mock(Predicate.class);
        Predicate soundtrackPredicate = mock(Predicate.class);
        Predicate carPredicate = mock(Predicate.class);
        Predicate realHeroPredicate = mock(Predicate.class);
        Predicate hasToothpickPredicate = mock(Predicate.class);
        Predicate weaponPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(builder.lower(namePath)).thenReturn(namePath);
        when(builder.lower(soundtrackPath)).thenReturn(soundtrackPath);
        when(builder.like(namePath, "%ivan%"))
                .thenReturn(namePredicate);
        when(builder.like(soundtrackPath, "song%"))
                .thenReturn(soundtrackPredicate);
        when(builder.equal(moodPath, Mood.SADNESS)).thenReturn(moodPredicate);
        when(builder.greaterThanOrEqualTo(impactSpeedPath, 10)).thenReturn(minPredicate);
        when(builder.lessThanOrEqualTo(impactSpeedPath, 20)).thenReturn(maxPredicate);
        when(builder.equal(carIdPath, 3L)).thenReturn(carPredicate);
        when(builder.isTrue(realHeroPath)).thenReturn(realHeroPredicate);
        when(builder.isFalse(hasToothpickPath)).thenReturn(hasToothpickPredicate);
        when(builder.equal(weaponTypePath, WeaponType.SHOTGUN)).thenReturn(weaponPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        Predicate result = HumanBeingSpecifications.withFilter(filter).toPredicate(root, query, builder);

        assertThat(result).isSameAs(combinedPredicate);
        verify(builder).like(namePath, "%ivan%");
        verify(builder).like(soundtrackPath, "song%");
        verify(builder).equal(moodPath, Mood.SADNESS);
        verify(builder).greaterThanOrEqualTo(impactSpeedPath, 10);
        verify(builder).lessThanOrEqualTo(impactSpeedPath, 20);
        verify(builder).equal(carIdPath, 3L);
        verify(builder).isTrue(realHeroPath);
        verify(builder).isFalse(hasToothpickPath);
        verify(builder).equal(weaponTypePath, WeaponType.SHOTGUN);

        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(captor.capture());
        Predicate[] predicates = captor.getValue();
        assertThat(predicates)
                .containsExactlyInAnyOrder(namePredicate, moodPredicate, minPredicate, maxPredicate,
                        soundtrackPredicate, carPredicate, realHeroPredicate, hasToothpickPredicate, weaponPredicate);
    }

    @Test
    void withFilterShouldReturnAlwaysTruePredicateWhenFilterEmpty() {
        Predicate combinedPredicate = mock(Predicate.class);
        when(builder.and(any(Predicate[].class))).thenReturn(combinedPredicate);

        Predicate result = HumanBeingSpecifications.withFilter(new HumanBeingFilter()).toPredicate(root, query, builder);

        assertThat(result).isSameAs(combinedPredicate);
        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(captor.capture());
        assertThat(captor.getValue()).isEmpty();
    }
}
