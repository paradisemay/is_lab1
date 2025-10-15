package ru.ifmo.se.is_lab1.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.domain.Coordinates;
import ru.ifmo.se.is_lab1.domain.HumanBeing;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;

@Configuration
@ConditionalOnProperty(prefix = "app.testdata", name = "enabled", havingValue = "true")
public class TestDataSeederConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TestDataSeederConfiguration.class);

    private static final String HUMAN_NAME_PREFIX = "🧪 Фильтр-тестер ";
    private static final String[] CODENAMES = {
        "Гравитационный Поэт",
        "Акустический Археолог",
        "Лунный Сомелье",
        "Магнитный Дипломат",
        "Гелиосный Хореограф",
        "Кометный Урбанист",
        "Туманностный Пекарь",
        "Орбитальный Садовник",
        "Плазменный Философ",
        "Хронодиджей"
    };

    private static final String FIRST_SEEDED_NAME = buildName(1);

    @Bean
    CommandLineRunner testDataLoader(HumanBeingRepository humanBeingRepository,
                                     CoordinatesRepository coordinatesRepository,
                                     CarRepository carRepository) {
        return args -> {
            if (humanBeingRepository.existsByName(FIRST_SEEDED_NAME)) {
                log.info("Тестовые данные уже присутствуют, генерация пропущена");
                return;
            }

            log.info("Генерируем 100 тестовых записей для отладки фильтров");

            List<Car> garage = carRepository.saveAll(createGarage());
            List<HumanBeing> crew = new ArrayList<>(100);

            WeaponType[] weapons = WeaponType.values();
            Mood[] moods = Mood.values();

            for (int index = 1; index <= 100; index++) {
                Coordinates coordinates = coordinatesRepository.save(createCoordinates(index));
                String name = buildName(index);
                Boolean realHero = index % 5 == 0 ? null : index % 2 == 0;
                boolean hasToothpick = index % 3 != 0;
                int impactSpeed = 47 + (index * 19) % 700;
                String soundtrack = String.format(Locale.ROOT,
                    "🎧 Сет %03d: %s",
                    index,
                    chooseSoundtrackTheme(index));
                WeaponType weapon = index % 7 == 0 ? null : weapons[(index - 1) % weapons.length];
                Mood mood = index % 4 == 0 ? null : moods[(index - 1) % moods.length];
                Car car = index % 5 == 0 ? null : garage.get((index - 1) % garage.size());

                HumanBeing human = new HumanBeing(
                    name,
                    coordinates,
                    realHero,
                    hasToothpick,
                    impactSpeed,
                    soundtrack,
                    weapon,
                    mood,
                    car
                );

                crew.add(human);
            }

            humanBeingRepository.saveAll(crew);
            log.info("Добавлено {} тестовых записей", crew.size());
        };
    }

    private static Coordinates createCoordinates(int index) {
        int magnitude = 15 + (index * 29) % 350;
        int sign = index % 2 == 0 ? 1 : -1;
        int x = sign * magnitude;
        float y = (float) (12.5 + (index * 7.3) % 420);
        return new Coordinates(x, y);
    }

    private static String chooseSoundtrackTheme(int index) {
        String[] themes = {
            "Альфа-ритмы астероидов",
            "Джазовая невесомость",
            "Северное сияние синтезаторов",
            "Космический даб",
            "Пульсарный хип-хоп",
            "Ритуал вакуумного техно",
            "Биолюминесцентный эмбиент",
            "Гравитационный шугейз",
            "Кристаллический чилл",
            "Метеоритный соул"
        };

        return themes[(index - 1) % themes.length];
    }

    private static List<Car> createGarage() {
        List<String> models = Arrays.asList(
            "Ховербайк \"Теория Шума\"",
            "Гравикар \"Фотонная Сардина\"",
            "Пульс-вагон \"Ритм Млечного Пути\"",
            "Орбитовоз \"Солнечный Самовар\"",
            "Радиолёт \"Субботний Квазар\"",
            "Метеоромобиль \"Пиксельный Макрокосм\"",
            "Плазменный дрэгстер \"Хрустальный Йо-йо\"",
            "Нейрокар \"Лаванда в невесомости\"",
            "Фотонетт \"Синкопированная Комета\"",
            "Квантовый скутер \"Миксер Гравитации\"",
            "Дарк-материйный лимузин \"Лунный Кот\"",
            "Саунд-срафт \"Дворцовый Дрон\""
        );

        return IntStream.range(0, models.size())
            .mapToObj(index -> new Car(
                "🚗 " + models.get(index),
                index % 2 == 0
            ))
            .collect(Collectors.toList());
    }

    private static String buildName(int index) {
        String codename = CODENAMES[(index - 1) % CODENAMES.length];
        return String.format(Locale.ROOT, "%s%03d «%s»", HUMAN_NAME_PREFIX, index, codename);
    }
}
