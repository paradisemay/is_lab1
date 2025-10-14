package ru.ifmo.se.is_lab1.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.ifmo.se.is_lab1.domain.MusicBand;
import ru.ifmo.se.is_lab1.dto.MusicBandFilter;

public final class MusicBandSpecifications {

    private MusicBandSpecifications() {
    }

    public static Specification<MusicBand> withFilter(MusicBandFilter filter) {
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

            filter.carIdOptional().ifPresent(carId -> predicates.add(builder.equal(root.get("car").get("id"), carId)));

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
