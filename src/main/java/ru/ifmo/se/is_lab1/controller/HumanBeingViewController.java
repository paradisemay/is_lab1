package ru.ifmo.se.is_lab1.controller;

import java.util.Arrays;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.service.CarService;
import ru.ifmo.se.is_lab1.service.HumanBeingService;

@Controller
@RequestMapping("/humans")
public class HumanBeingViewController {

    private final HumanBeingService humanBeingService;
    private final CarService carService;

    public HumanBeingViewController(HumanBeingService humanBeingService, CarService carService) {
        this.humanBeingService = humanBeingService;
        this.carService = carService;
    }

    @GetMapping
    public String list(@RequestParam(name = "page") Optional<Integer> page,
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
                       @RequestParam(name = "hasToothpick") Optional<Boolean> hasToothpick,
                       Model model) {
        int pageNumber = page.orElse(0);
        int pageSize = size.orElse(10);
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

        Page<HumanBeingDto> pageResult = humanBeingService.findAll(filter, pageable);

        model.addAttribute("humans", pageResult);
        model.addAttribute("moods", Arrays.asList(Mood.values()));
        model.addAttribute("weaponTypes", Arrays.asList(WeaponType.values()));
        model.addAttribute("cars", carService.findAll());
        model.addAttribute("sumImpactSpeed", humanBeingService.sumImpactSpeed());
        model.addAttribute("filter", filter);
        model.addAttribute("page", pageNumber);
        model.addAttribute("size", pageSize);
        model.addAttribute("sort", sort.orElse("id"));
        model.addAttribute("direction", direction.orElse("ASC"));
        return "humans/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("human")) {
            HumanBeingFormDto form = new HumanBeingFormDto();
            form.setHasToothpick(Boolean.FALSE);
            model.addAttribute("human", form);
        }
        populateReferenceData(model);
        return "humans/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("human") HumanBeingFormDto form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.human", bindingResult);
            redirectAttributes.addFlashAttribute("human", form);
            return "redirect:/humans/create";
        }
        HumanBeingDto dto = humanBeingService.create(form);
        redirectAttributes.addFlashAttribute("success", "Человек создан");
        return "redirect:/humans/" + dto.getId();
    }

    @GetMapping("/{id}")
    public String view(@PathVariable("id") Long id, Model model) {
        HumanBeingDto dto = humanBeingService.findById(id);
        model.addAttribute("human", dto);
        return "humans/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        if (!model.containsAttribute("human")) {
            HumanBeingDto dto = humanBeingService.findById(id);
            HumanBeingFormDto form = new HumanBeingFormDto();
            form.setName(dto.getName());
            if (dto.getCoordinates() != null) {
                form.setCoordinatesX(dto.getCoordinates().getX());
                form.setCoordinatesY(dto.getCoordinates().getY());
            }
            form.setRealHero(dto.getRealHero());
            form.setHasToothpick(dto.isHasToothpick());
            form.setImpactSpeed(dto.getImpactSpeed());
            form.setSoundtrackName(dto.getSoundtrackName());
            form.setWeaponType(dto.getWeaponType());
            form.setMood(dto.getMood());
            if (dto.getCar() != null) {
                form.setCarId(dto.getCar().getId());
            }
            model.addAttribute("human", form);
        }
        populateReferenceData(model);
        model.addAttribute("humanId", id);
        return "humans/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("human") HumanBeingFormDto form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.human", bindingResult);
            redirectAttributes.addFlashAttribute("human", form);
            return "redirect:/humans/" + id + "/edit";
        }
        humanBeingService.update(id, form);
        redirectAttributes.addFlashAttribute("success", "Изменения сохранены");
        return "redirect:/humans/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        humanBeingService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Человек удалён");
        return "redirect:/humans";
    }

    private void populateReferenceData(Model model) {
        model.addAttribute("moods", Arrays.asList(Mood.values()));
        model.addAttribute("weaponTypes", Arrays.asList(WeaponType.values()));
        model.addAttribute("cars", carService.findAll());
    }
}
