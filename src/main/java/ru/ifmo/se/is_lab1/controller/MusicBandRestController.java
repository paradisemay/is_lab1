package ru.ifmo.se.is_lab1.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.is_lab1.domain.Mood;
import ru.ifmo.se.is_lab1.dto.AssignCarRequest;
import ru.ifmo.se.is_lab1.dto.MoodChangeRequest;
import ru.ifmo.se.is_lab1.dto.MusicBandDto;
import ru.ifmo.se.is_lab1.dto.MusicBandFilter;
import ru.ifmo.se.is_lab1.dto.MusicBandFormDto;
import ru.ifmo.se.is_lab1.service.MusicBandService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bands")
public class MusicBandRestController {

    private final MusicBandService musicBandService;

    public MusicBandRestController(MusicBandService musicBandService) {
        this.musicBandService = musicBandService;
    }

    @GetMapping
    public Page<MusicBandDto> findAll(@RequestParam(name = "page") Optional<Integer> page,
                                      @RequestParam(name = "size") Optional<Integer> size,
                                      @RequestParam(name = "sort") Optional<String> sort,
                                      @RequestParam(name = "direction") Optional<String> direction,
                                      @RequestParam(name = "name") Optional<String> name,
                                      @RequestParam(name = "mood") Optional<Mood> mood,
                                      @RequestParam(name = "minImpactSpeed") Optional<BigDecimal> minImpactSpeed,
                                      @RequestParam(name = "maxImpactSpeed") Optional<BigDecimal> maxImpactSpeed,
                                      @RequestParam(name = "soundtrackPrefix") Optional<String> soundtrackPrefix,
                                      @RequestParam(name = "carId") Optional<Long> carId) {
        int pageNumber = page.orElse(0);
        int pageSize = size.orElse(20);
        Sort sortOrder = sort.map(property -> Sort.by(direction.map(Sort.Direction::fromString).orElse(Sort.Direction.ASC), property))
                .orElse(Sort.by(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortOrder);

        MusicBandFilter filter = new MusicBandFilter();
        name.ifPresent(filter::setName);
        mood.ifPresent(filter::setMood);
        minImpactSpeed.ifPresent(filter::setMinImpactSpeed);
        maxImpactSpeed.ifPresent(filter::setMaxImpactSpeed);
        soundtrackPrefix.ifPresent(filter::setSoundtrackPrefix);
        carId.ifPresent(filter::setCarId);
        return musicBandService.findAll(filter, pageable);
    }

    @GetMapping("/{id}")
    public MusicBandDto findById(@PathVariable("id") Long id) {
        return musicBandService.findById(id);
    }

    @PostMapping
    public MusicBandDto create(@Valid @RequestBody MusicBandFormDto form) {
        return musicBandService.create(form);
    }

    @PutMapping("/{id}")
    public MusicBandDto update(@PathVariable("id") Long id, @Valid @RequestBody MusicBandFormDto form) {
        return musicBandService.update(id, form);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        musicBandService.delete(id);
    }

    @GetMapping("/impact-speed/sum")
    public BigDecimal sumImpactSpeed() {
        return musicBandService.sumImpactSpeed();
    }

    @GetMapping("/soundtrack")
    public List<MusicBandDto> findBySoundtrackPrefix(@RequestParam("prefix") String prefix) {
        return musicBandService.findBySoundtrackPrefix(prefix);
    }

    @PostMapping("/mood/bulk")
    public int bulkUpdateMood(@Valid @RequestBody MoodChangeRequest request) {
        return musicBandService.bulkUpdateMood(request.getSourceMood(), request.getTargetMood());
    }

    @PostMapping("/{id}/assign-car")
    public MusicBandDto assignCar(@PathVariable("id") Long id, @Valid @RequestBody AssignCarRequest request) {
        return musicBandService.assignCar(id, request.getCarId());
    }
}
