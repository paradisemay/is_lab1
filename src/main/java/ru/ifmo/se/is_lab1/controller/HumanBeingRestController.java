package ru.ifmo.se.is_lab1.controller;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
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

@RestController
@Validated
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
                                       @RequestParam(name = "minImpactSpeed") Optional<Integer> minImpactSpeed,
                                       @RequestParam(name = "maxImpactSpeed") Optional<Integer> maxImpactSpeed,
                                       @RequestParam(name = "soundtrackPrefix") Optional<String> soundtrackPrefix,
                                       @RequestParam(name = "carId") Optional<Long> carId,
                                       @RequestParam(name = "realHero") Optional<Boolean> realHero,
                                       @RequestParam(name = "hasToothpick") Optional<Boolean> hasToothpick) {
        int pageNumber = page.orElse(0);
        int pageSize = size.orElse(20);
        Sort sortOrder = sort.map(property -> Sort.by(direction.map(Sort.Direction::fromString).orElse(Sort.Direction.ASC), property))
                .orElse(Sort.by(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortOrder);

        HumanBeingFilter filter = new HumanBeingFilter();
        name.ifPresent(filter::setName);
        mood.ifPresent(filter::setMood);
        weaponType.ifPresent(filter::setWeaponType);
        minImpactSpeed.ifPresent(filter::setMinImpactSpeed);
        maxImpactSpeed.ifPresent(filter::setMaxImpactSpeed);
        soundtrackPrefix.ifPresent(filter::setSoundtrackPrefix);
        carId.ifPresent(filter::setCarId);
        realHero.ifPresent(filter::setRealHero);
        hasToothpick.ifPresent(filter::setHasToothpick);
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
    public long sumImpactSpeed() {
        return humanBeingService.sumImpactSpeed();
    }

    @GetMapping("/impact-speed/count")
    public long countByImpactSpeedLessThan(@RequestParam("threshold")
                                           @NotNull(message = "Порог обязателен")
                                           @Min(value = 1, message = "Порог должен быть положительным")
                                           @Max(value = 907, message = "Максимальное значение скорости удара — 907") Integer threshold) {
        return humanBeingService.countByImpactSpeedLessThan(threshold);
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

    @PostMapping("/mood/gloom")
    public int updateMoodToGloom() {
        return humanBeingService.updateMoodToGloom();
    }

    @PostMapping("/cars/assign-default")
    public int assignDefaultCar() {
        return humanBeingService.assignDefaultCarToHeroesWithoutCar();
    }
}
