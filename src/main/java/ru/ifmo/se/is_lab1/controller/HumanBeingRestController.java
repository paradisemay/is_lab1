package ru.ifmo.se.is_lab1.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ifmo.se.is_lab1.dto.AssignCarRequest;
import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.dto.MoodChangeRequest;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.service.HumanBeingService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/humans")
public class HumanBeingRestController {

    private final HumanBeingService humanBeingService;

    public HumanBeingRestController(HumanBeingService humanBeingService) {
        this.humanBeingService = humanBeingService;
    }

    @GetMapping
    public Page<HumanBeingDto> findAll(@RequestParam(name = "page") Optional<Integer> page,
                                       @RequestParam(name = "size") Optional<Integer> size,
                                       @RequestParam(name = "sort") Optional<String> sort,
                                       @RequestParam(name = "direction") Optional<String> direction,
                                       @RequestParam(name = "name") Optional<String> name,
                                       @RequestParam(name = "mood") Optional<Mood> mood,
                                       @RequestParam(name = "weaponType") Optional<WeaponType> weaponType,
                                       @RequestParam(name = "realHero") Optional<Boolean> realHero,
                                       @RequestParam(name = "hasToothpick") Optional<Boolean> hasToothpick,
                                       @RequestParam(name = "minImpactSpeed") Optional<Double> minImpactSpeed,
                                       @RequestParam(name = "maxImpactSpeed") Optional<Double> maxImpactSpeed,
                                       @RequestParam(name = "minMinutesOfWaiting") Optional<Long> minMinutesOfWaiting,
                                       @RequestParam(name = "maxMinutesOfWaiting") Optional<Long> maxMinutesOfWaiting,
                                       @RequestParam(name = "carId") Optional<Long> carId) {
        int pageNumber = page.orElse(0);
        int pageSize = size.orElse(20);
        Sort sortOrder = sort.map(property -> Sort.by(direction.map(Sort.Direction::fromString).orElse(Sort.Direction.ASC), property))
                .orElse(Sort.by(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortOrder);

        HumanBeingFilter filter = new HumanBeingFilter();
        name.ifPresent(filter::setName);
        mood.ifPresent(filter::setMood);
        weaponType.ifPresent(filter::setWeaponType);
        realHero.ifPresent(filter::setRealHero);
        hasToothpick.ifPresent(filter::setHasToothpick);
        minImpactSpeed.ifPresent(filter::setMinImpactSpeed);
        maxImpactSpeed.ifPresent(filter::setMaxImpactSpeed);
        minMinutesOfWaiting.ifPresent(filter::setMinMinutesOfWaiting);
        maxMinutesOfWaiting.ifPresent(filter::setMaxMinutesOfWaiting);
        carId.ifPresent(filter::setCarId);

        return humanBeingService.findAll(filter, pageable);
    }

    @GetMapping("/{id}")
    public HumanBeingDto findById(@PathVariable("id") Long id) {
        return humanBeingService.findById(id);
    }

    @PostMapping
    public HumanBeingDto create(@Valid @RequestBody HumanBeingFormDto form) {
        return humanBeingService.create(form);
    }

    @PutMapping("/{id}")
    public HumanBeingDto update(@PathVariable("id") Long id, @Valid @RequestBody HumanBeingFormDto form) {
        return humanBeingService.update(id, form);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        humanBeingService.delete(id);
    }

    @GetMapping("/impact-speed/sum")
    public double sumImpactSpeed() {
        return humanBeingService.sumImpactSpeed();
    }

    @GetMapping("/soundtrack")
    public List<HumanBeingDto> findBySoundtrackPrefix(@RequestParam("prefix") String prefix) {
        return humanBeingService.findBySoundtrackPrefix(prefix);
    }

    @PostMapping("/mood/bulk")
    public int bulkUpdateMood(@Valid @RequestBody MoodChangeRequest request) {
        return humanBeingService.bulkUpdateMood(request.getSourceMood(), request.getTargetMood());
    }

    @PostMapping("/{id}/assign-car")
    public HumanBeingDto assignCar(@PathVariable("id") Long id, @Valid @RequestBody AssignCarRequest request) {
        return humanBeingService.assignCar(id, request.getCarId());
    }
}
