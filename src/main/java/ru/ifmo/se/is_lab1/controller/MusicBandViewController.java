package ru.ifmo.se.is_lab1.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.ifmo.se.is_lab1.domain.Mood;
import ru.ifmo.se.is_lab1.dto.MusicBandDto;
import ru.ifmo.se.is_lab1.dto.MusicBandFilter;
import ru.ifmo.se.is_lab1.dto.MusicBandFormDto;
import ru.ifmo.se.is_lab1.service.CarService;
import ru.ifmo.se.is_lab1.service.MusicBandService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

@Controller
@RequestMapping("/bands")
public class MusicBandViewController {

    private final MusicBandService musicBandService;
    private final CarService carService;

    public MusicBandViewController(MusicBandService musicBandService, CarService carService) {
        this.musicBandService = musicBandService;
        this.carService = carService;
    }

    @GetMapping
    public String list(@RequestParam(name = "page") Optional<Integer> page,
                       @RequestParam(name = "size") Optional<Integer> size,
                       @RequestParam(name = "sort") Optional<String> sort,
                       @RequestParam(name = "direction") Optional<String> direction,
                       @RequestParam(name = "name") Optional<String> name,
                       @RequestParam(name = "mood") Optional<Mood> mood,
                       @RequestParam(name = "minImpactSpeed") Optional<BigDecimal> minImpactSpeed,
                       @RequestParam(name = "maxImpactSpeed") Optional<BigDecimal> maxImpactSpeed,
                       @RequestParam(name = "soundtrackPrefix") Optional<String> soundtrackPrefix,
                       @RequestParam(name = "carId") Optional<Long> carId,
                       Model model) {
        int pageNumber = page.orElse(0);
        int pageSize = size.orElse(10);
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

        Page<MusicBandDto> pageResult = musicBandService.findAll(filter, pageable);

        model.addAttribute("bands", pageResult);
        model.addAttribute("moods", Arrays.asList(Mood.values()));
        model.addAttribute("cars", carService.findAll());
        model.addAttribute("sumImpactSpeed", musicBandService.sumImpactSpeed());
        model.addAttribute("filter", filter);
        model.addAttribute("page", pageNumber);
        model.addAttribute("size", pageSize);
        model.addAttribute("sort", sort.orElse("id"));
        model.addAttribute("direction", direction.orElse("ASC"));
        return "bands/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("band")) {
            model.addAttribute("band", new MusicBandFormDto());
        }
        model.addAttribute("moods", Arrays.asList(Mood.values()));
        model.addAttribute("cars", carService.findAll());
        return "bands/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("band") MusicBandFormDto form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.band", bindingResult);
            redirectAttributes.addFlashAttribute("band", form);
            return "redirect:/bands/create";
        }
        MusicBandDto dto = musicBandService.create(form);
        redirectAttributes.addFlashAttribute("success", "Группа создана");
        return "redirect:/bands/" + dto.getId();
    }

    @GetMapping("/{id}")
    public String view(@PathVariable("id") Long id, Model model) {
        MusicBandDto dto = musicBandService.findById(id);
        model.addAttribute("band", dto);
        return "bands/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        if (!model.containsAttribute("band")) {
            MusicBandDto dto = musicBandService.findById(id);
            MusicBandFormDto form = new MusicBandFormDto();
            form.setName(dto.getName());
            form.setImpactSpeed(dto.getImpactSpeed());
            form.setSoundtrackName(dto.getSoundtrackName());
            form.setMood(dto.getMood());
            if (dto.getCar() != null) {
                form.setCarId(dto.getCar().getId());
            }
            model.addAttribute("band", form);
        }
        model.addAttribute("moods", Arrays.asList(Mood.values()));
        model.addAttribute("cars", carService.findAll());
        model.addAttribute("bandId", id);
        return "bands/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("band") MusicBandFormDto form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.band", bindingResult);
            redirectAttributes.addFlashAttribute("band", form);
            return "redirect:/bands/" + id + "/edit";
        }
        musicBandService.update(id, form);
        redirectAttributes.addFlashAttribute("success", "Изменения сохранены");
        return "redirect:/bands/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        musicBandService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Группа удалена");
        return "redirect:/bands";
    }
}
