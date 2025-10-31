package ru.ifmo.se.is_lab1.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

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
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.dto.ImpactSpeedCountRequest;
import ru.ifmo.se.is_lab1.dto.MoodChangeRequest;
import ru.ifmo.se.is_lab1.dto.SoundtrackSearchRequest;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.dto.ImportOperationDto;
import ru.ifmo.se.is_lab1.service.CarService;
import ru.ifmo.se.is_lab1.service.HumanBeingService;
import ru.ifmo.se.is_lab1.service.ImportOperationService;
import ru.ifmo.se.is_lab1.service.security.CurrentUserService;
import ru.ifmo.se.is_lab1.service.exception.HumanBeingDeletionException;
import ru.ifmo.se.is_lab1.service.exception.HumanBeingUniquenessException;

@Controller
@RequestMapping("/humans")
public class HumanBeingViewController {

    private final HumanBeingService humanBeingService;
    private final CarService carService;
    private final ImportOperationService importOperationService;
    private final CurrentUserService currentUserService;

    public HumanBeingViewController(HumanBeingService humanBeingService,
                                    CarService carService,
                                    ImportOperationService importOperationService,
                                    CurrentUserService currentUserService) {
        this.humanBeingService = humanBeingService;
        this.carService = carService;
        this.importOperationService = importOperationService;
        this.currentUserService = currentUserService;
    }

    @ModelAttribute("moodChangeRequest")
    public MoodChangeRequest moodChangeRequest() {
        return new MoodChangeRequest();
    }

    @ModelAttribute("impactSpeedRequest")
    public ImpactSpeedCountRequest impactSpeedRequest() {
        return new ImpactSpeedCountRequest();
    }

    @ModelAttribute("soundtrackSearchRequest")
    public SoundtrackSearchRequest soundtrackSearchRequest() {
        return new SoundtrackSearchRequest();
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

        ModelAndView mav = new ModelAndView("humans/list");
        mav.addObject("humans", pageResult);
        mav.addObject("sumImpactSpeed", humanBeingService.sumImpactSpeed());
        mav.addObject("filter", filter);
        mav.addObject("page", pageNumber);
        mav.addObject("size", pageSize);
        mav.addObject("sort", sort.orElse("id"));
        mav.addObject("direction", direction.orElse("ASC"));

        if (!mav.getModel().containsKey("moodChangeRequest")) {
            mav.addObject("moodChangeRequest", new MoodChangeRequest());
        }
        if (!mav.getModel().containsKey("impactSpeedRequest")) {
            mav.addObject("impactSpeedRequest", new ImpactSpeedCountRequest());
        }
        if (!mav.getModel().containsKey("soundtrackSearchRequest")) {
            mav.addObject("soundtrackSearchRequest", new SoundtrackSearchRequest());
        }

        populateReferenceData(mav);
        Page<ImportOperationDto> historyPage = importOperationService.findHistory(PageRequest.of(0, 10));
        mav.addObject("importEndpoint", "/api/humans/import");
        mav.addObject("importHistoryEndpoint", "/api/humans/import/history");
        mav.addObject("importHistory", historyPage);
        mav.addObject("historyIsAdmin", currentUserService.isAdmin());
        mav.addObject("historyUsername", currentUserService.getCurrentUsername());
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
        populateReferenceData(mav.getModelMap());
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
            populateReferenceData(mav.getModelMap());
            return mav;
        }
        try {
            HumanBeingDto dto = humanBeingService.create(form);
            redirectAttributes.addFlashAttribute("success", "Человек создан");
            return new ModelAndView("redirect:/humans/" + dto.getId());
        } catch (HumanBeingUniquenessException ex) {
            bindingResult.reject("human.unique", ex.getMessage());
            ModelAndView mav = new ModelAndView("humans/create");
            mav.addObject("human", form);
            mav.addObject(BindingResult.MODEL_KEY_PREFIX + "human", bindingResult);
            populateReferenceData(mav.getModelMap());
            return mav;
        }
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
        populateReferenceData(mav.getModelMap());
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
            populateReferenceData(mav.getModelMap());
            return mav;
        }
        try {
            humanBeingService.update(id, form);
            redirectAttributes.addFlashAttribute("success", "Изменения сохранены");
            return new ModelAndView("redirect:/humans/" + id);
        } catch (HumanBeingUniquenessException ex) {
            bindingResult.reject("human.unique", ex.getMessage());
            ModelAndView mav = new ModelAndView("humans/edit");
            mav.addObject("human", form);
            mav.addObject("humanId", id);
            mav.addObject(BindingResult.MODEL_KEY_PREFIX + "human", bindingResult);
            populateReferenceData(mav.getModelMap());
            return mav;
        }
    }

    @PostMapping("/{id}/delete")
    public ModelAndView delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            humanBeingService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Человек удалён");
            return new ModelAndView("redirect:/humans");
        } catch (HumanBeingDeletionException ex) {
            redirectAttributes.addFlashAttribute("deleteError", ex.getMessage());
            return new ModelAndView("redirect:/humans/" + id + "/edit");
        }
    }

    @PostMapping("/actions/change-mood")
    public String changeMood(@Valid @ModelAttribute("moodChangeRequest") MoodChangeRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.moodChangeRequest", bindingResult);
            redirectAttributes.addFlashAttribute("moodChangeRequest", request);
            redirectAttributes.addFlashAttribute("actionError", "Укажите исходное и новое настроение");
            return "redirect:/humans";
        }
        int updated = humanBeingService.bulkUpdateMood(request.getSourceMood(), request.getTargetMood());
        if (updated > 0) {
            redirectAttributes.addFlashAttribute("success", String.format("Изменено настроение у %d человек", updated));
        } else {
            redirectAttributes.addFlashAttribute("actionError", "Подходящих людей не найдено");
        }
        return "redirect:/humans";
    }

    @PostMapping("/actions/make-gloom")
    public String makeEveryoneGloomy(RedirectAttributes redirectAttributes) {
        int updated = humanBeingService.updateMoodToGloom();
        if (updated > 0) {
            redirectAttributes.addFlashAttribute("success", String.format("Настроение %d человек установлено в GLOOM", updated));
        } else {
            redirectAttributes.addFlashAttribute("actionError", "Настроения уже установлены в GLOOM");
        }
        return "redirect:/humans";
    }

    @PostMapping("/actions/assign-default-car")
    public String assignDefaultCar(RedirectAttributes redirectAttributes) {
        int updated = humanBeingService.assignDefaultCarToHeroesWithoutCar();
        if (updated > 0) {
            redirectAttributes.addFlashAttribute("success", String.format("Автомобиль назначен %d героям", updated));
        } else {
            redirectAttributes.addFlashAttribute("actionError", "Все герои уже с автомобилями");
        }
        return "redirect:/humans";
    }

    @PostMapping("/actions/count-impact")
    public String countByImpactSpeed(@Valid @ModelAttribute("impactSpeedRequest") ImpactSpeedCountRequest request,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.impactSpeedRequest", bindingResult);
            redirectAttributes.addFlashAttribute("impactSpeedRequest", request);
            redirectAttributes.addFlashAttribute("actionError", "Исправьте ошибки в пороге скорости");
            return "redirect:/humans";
        }
        long count = humanBeingService.countByImpactSpeedLessThan(request.getThreshold());
        redirectAttributes.addFlashAttribute("impactSpeedCount", count);
        redirectAttributes.addFlashAttribute("impactSpeedRequest", request);
        return "redirect:/humans";
    }

    @PostMapping("/actions/search-soundtrack")
    public String searchBySoundtrack(@Valid @ModelAttribute("soundtrackSearchRequest") SoundtrackSearchRequest request,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.soundtrackSearchRequest", bindingResult);
            redirectAttributes.addFlashAttribute("soundtrackSearchRequest", request);
            redirectAttributes.addFlashAttribute("actionError", "Префикс саундтрека обязателен");
            return "redirect:/humans";
        }
        List<HumanBeingDto> matches = humanBeingService.findBySoundtrackPrefix(request.getPrefix());
        redirectAttributes.addFlashAttribute("soundtrackMatches", matches);
        redirectAttributes.addFlashAttribute("soundtrackSearchRequest", request);
        if (matches.isEmpty()) {
            redirectAttributes.addFlashAttribute("actionError", "Совпадения по саундтреку не найдены");
        } else {
            redirectAttributes.addFlashAttribute("success", String.format("Найдено %d совпадений по саундтрекам", matches.size()));
        }
        return "redirect:/humans";
    }

    private void populateReferenceData(Model model) {
        populateReferenceData(model::addAttribute);
    }

    private void populateReferenceData(ModelMap model) {
        populateReferenceData(model::addAttribute);
    }

    private void populateReferenceData(BiConsumer<String, Object> adder) {
        adder.accept("moods", Arrays.asList(Mood.values()));
        adder.accept("weaponTypes", Arrays.asList(WeaponType.values()));
        adder.accept("cars", carService.findAll());
    }
    
    private void populateReferenceData(ModelAndView modelAndView) {
        modelAndView.addObject("moods", Arrays.asList(Mood.values()));
        modelAndView.addObject("weaponTypes", Arrays.asList(WeaponType.values()));
        modelAndView.addObject("cars", carService.findAll());
    }
}
