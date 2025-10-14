package ru.ifmo.se.is_lab1.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.is_lab1.domain.Mood;
import ru.ifmo.se.is_lab1.dto.MusicBandDto;
import ru.ifmo.se.is_lab1.dto.MusicBandFilter;
import ru.ifmo.se.is_lab1.dto.MusicBandFormDto;
import ru.ifmo.se.is_lab1.service.event.MusicBandEventPublisher;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MusicBandServiceTest {

    @Autowired
    private MusicBandService musicBandService;

    @Autowired
    private CarService carService;

    @MockBean
    private MusicBandEventPublisher eventPublisher;

    @Test
    void createBandAndCalculateSum() {
        var car = carService.create("Volga", "GAZ-24", "Белый");

        MusicBandFormDto form = new MusicBandFormDto();
        form.setName("Aurora");
        form.setImpactSpeed(new BigDecimal("12.50"));
        form.setSoundtrackName("Northern Lights");
        form.setMood(Mood.CALM);
        form.setCarId(car.getId());

        MusicBandDto created = musicBandService.create(form);

        assertThat(created.getId()).isNotNull();
        assertThat(musicBandService.sumImpactSpeed()).isEqualByComparingTo("12.50");
        assertThat(created.getCar()).isNotNull();
        assertThat(created.getCar().getName()).isEqualTo("Volga");
    }

    @Test
    void filterAndBulkUpdateMood() {
        MusicBandFormDto first = new MusicBandFormDto();
        first.setName("Thunder");
        first.setImpactSpeed(new BigDecimal("9.00"));
        first.setSoundtrackName("Storm Rising");
        first.setMood(Mood.ANGRY);
        musicBandService.create(first);

        MusicBandFormDto second = new MusicBandFormDto();
        second.setName("Sunny");
        second.setImpactSpeed(new BigDecimal("5.00"));
        second.setSoundtrackName("Sunrise Melody");
        second.setMood(Mood.SAD);
        musicBandService.create(second);

        MusicBandFilter filter = new MusicBandFilter();
        filter.setSoundtrackPrefix("Sun");

        List<MusicBandDto> filtered = musicBandService.findAll(filter, org.springframework.data.domain.PageRequest.of(0, 10))
                .getContent();
        assertThat(filtered).hasSize(1);
        assertThat(filtered.getFirst().getName()).isEqualTo("Sunny");

        int updated = musicBandService.bulkUpdateMood(Mood.ANGRY, Mood.HAPPY);
        assertThat(updated).isEqualTo(1);

        List<MusicBandDto> byPrefix = musicBandService.findBySoundtrackPrefix("Sto");
        assertThat(byPrefix).hasSize(1);
        assertThat(byPrefix.getFirst().getMood()).isEqualTo(Mood.HAPPY);
    }
}
