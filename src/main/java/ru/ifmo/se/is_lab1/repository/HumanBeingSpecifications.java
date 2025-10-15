package ru.ifmo.se.is_lab1.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.ifmo.se.is_lab1.domain.HumanBeing;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;

public final class HumanBeingSpecifications {

    private HumanBeingSpecifications() {
    }

    public static Specification<HumanBeing> withFilter(HumanBeingFilter filter) {
        return (root, query, builder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            filter.nameOptional().ifPresent(name ->
                    predicates.add(builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%")));

            filter.moodOptional().ifPresent(mood -> predicates.add(builder.equal(root.get("mood"), mood)));

            filter.minImpactSpeedOptional().ifPresent(min ->
                    predicates.add(builder.greaterThanOrEqualTo(root.get("impactSpeed"), min)));

            filter.maxImpactSpeedOptional().ifPresent(max ->
                    predicates.add(builder.lessThanOrEqualTo(root.get("impactSpeed"), max)));

            filter.soundtrackPrefixOptional().ifPresent(prefix ->
                    predicates.add(builder.like(builder.lower(root.get("soundtrackName")), prefix.toLowerCase() + "%")));

            filter.carIdOptional().ifPresent(carId ->
                    predicates.add(builder.equal(root.get("car").get("id"), carId)));

            filter.realHeroOptional().ifPresent(realHero ->
                    predicates.add(realHero
                            ? builder.isTrue(root.get("realHero"))
                            : builder.isFalse(root.get("realHero"))));

            filter.hasToothpickOptional().ifPresent(hasToothpick ->
                    predicates.add(hasToothpick
                            ? builder.isTrue(root.get("hasToothpick"))
                            : builder.isFalse(root.get("hasToothpick"))));

            filter.weaponTypeOptional().ifPresent(weaponType ->
                    predicates.add(builder.equal(root.get("weaponType"), weaponType)));

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
