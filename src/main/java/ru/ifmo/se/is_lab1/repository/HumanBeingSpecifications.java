package ru.ifmo.se.is_lab1.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.model.HumanBeing;

import java.util.ArrayList;

public final class HumanBeingSpecifications {

    private HumanBeingSpecifications() {
    }

    public static Specification<HumanBeing> withFilter(HumanBeingFilter filter) {
        return (root, query, builder) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();

            filter.nameOptional().ifPresent(name ->
                    predicates.add(builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%")));

            filter.moodOptional().ifPresent(mood ->
                    predicates.add(builder.equal(root.get("mood"), mood)));

            filter.weaponTypeOptional().ifPresent(weaponType ->
                    predicates.add(builder.equal(root.get("weaponType"), weaponType)));

            filter.realHeroOptional().ifPresent(realHero ->
                    predicates.add(builder.equal(root.get("realHero"), realHero)));

            filter.hasToothpickOptional().ifPresent(hasToothpick ->
                    predicates.add(builder.equal(root.get("hasToothpick"), hasToothpick)));

            filter.minImpactSpeedOptional().ifPresent(min ->
                    predicates.add(builder.greaterThanOrEqualTo(root.get("impactSpeed"), min)));

            filter.maxImpactSpeedOptional().ifPresent(max ->
                    predicates.add(builder.lessThanOrEqualTo(root.get("impactSpeed"), max)));

            filter.minMinutesOfWaitingOptional().ifPresent(min ->
                    predicates.add(builder.greaterThanOrEqualTo(root.get("minutesOfWaiting"), min)));

            filter.maxMinutesOfWaitingOptional().ifPresent(max ->
                    predicates.add(builder.lessThanOrEqualTo(root.get("minutesOfWaiting"), max)));

            filter.carIdOptional().ifPresent(carId ->
                    predicates.add(builder.equal(root.get("car").get("id"), carId)));

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
