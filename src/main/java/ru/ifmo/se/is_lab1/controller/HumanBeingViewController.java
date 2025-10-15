package ru.ifmo.se.is_lab1.controller;

import java.util.Arrays;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
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
    public ModelAndView list(@RequestParam(name = "page") Optional<Integer> page,
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

        ModelAndView mav = new ModelAndView("humans/list");
        mav.addObject("humans", pageResult);
        mav.addObject("moods", Arrays.asList(Mood.values()));
        mav.addObject("weaponTypes", Arrays.asList(WeaponType.values()));
        mav.addObject("cars", carService.findAll());
        mav.addObject("sumImpactSpeed", humanBeingService.sumImpactSpeed());
        mav.addObject("filter", filter);
        mav.addObject("page", pageNumber);
        mav.addObject("size", pageSize);
        mav.addObject("sort", sort.orElse("id"));
        mav.addObject("direction", direction.orElse("ASC"));
        return mav;
    }

    @GetMapping("/create")
    public ModelAndView createForm(@ModelAttribute("human") HumanBeingFormDto form,
                                   BindingResult bindingResult) {
        if (form.getHasToothpick() == null) {
            form.setHasToothpick(Boolean.FALSE);
        }
        ModelAndView mav = new ModelAndView("humans/create");
        if (bindingResult.hasErrors()) {
            mav.addObject(BindingResult.MODEL_KEY_PREFIX + "human", bindingResult);
        }
        mav.addObject("human", form);
        populateReferenceData(mav);
        return mav;
    }

    @PostMapping
    public ModelAndView create(@Valid @ModelAttribute("human") HumanBeingFormDto form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("humans/create");
            mav.addObject("human", form);
            mav.addObject(BindingResult.MODEL_KEY_PREFIX + "human", bindingResult);
            populateReferenceData(mav);
            return mav;
        }
        HumanBeingDto dto = humanBeingService.create(form);
        redirectAttributes.addFlashAttribute("success", "Человек создан");
        return new ModelAndView("redirect:/humans/" + dto.getId());
    }

    @GetMapping("/{id}")
    public ModelAndView view(@PathVariable("id") Long id) {
        HumanBeingDto dto = humanBeingService.findById(id);
        ModelAndView mav = new ModelAndView("humans/view");
        mav.addObject("human", dto);
        return mav;
    }

    @GetMapping("/{id}/edit")
    public ModelAndView editForm(@PathVariable("id") Long id,
                                 @ModelAttribute("human") HumanBeingFormDto form,
                                 BindingResult bindingResult) {
        if (!bindingResult.hasErrors() && form.getName() == null) {
            HumanBeingDto dto = humanBeingService.findById(id);
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
        }
        ModelAndView mav = new ModelAndView("humans/edit");
        if (bindingResult.hasErrors()) {
            mav.addObject(BindingResult.MODEL_KEY_PREFIX + "human", bindingResult);
        }
        mav.addObject("human", form);
        mav.addObject("humanId", id);
        populateReferenceData(mav);
        return mav;
    }

    @PostMapping("/{id}")
    public ModelAndView update(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("human") HumanBeingFormDto form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("humans/edit");
            mav.addObject("human", form);
            mav.addObject("humanId", id);
            mav.addObject(BindingResult.MODEL_KEY_PREFIX + "human", bindingResult);
            populateReferenceData(mav);
            return mav;
        }
        humanBeingService.update(id, form);
        redirectAttributes.addFlashAttribute("success", "Изменения сохранены");
        return new ModelAndView("redirect:/humans/" + id);
    }

    @PostMapping("/{id}/delete")
    public ModelAndView delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        humanBeingService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Человек удалён");
        return new ModelAndView("redirect:/humans");
    }

    private void populateReferenceData(ModelAndView mav) {
        mav.addObject("moods", Arrays.asList(Mood.values()));
        mav.addObject("weaponTypes", Arrays.asList(WeaponType.values()));
        mav.addObject("cars", carService.findAll());
    }
}
