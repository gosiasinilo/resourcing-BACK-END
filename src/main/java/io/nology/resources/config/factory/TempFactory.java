package io.nology.resources.config.factory;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import io.nology.resources.common.services.LocationService;
import io.nology.resources.skill.entity.Skill;
import io.nology.resources.temp.entity.Temp;

@Component
@Profile({ "dev", "test" })
public class TempFactory {

    private final Faker faker = new Faker();
    private final LocationService locationService;

    public TempFactory(LocationService locationService) {
        this.locationService = locationService;
    }

    public Temp createTemp(List<Skill> skills) {
        String city = faker.options().option(
                "Melbourne", "Sydney", "Brisbane", "Perth", "Adelaide");

        double[] coords = locationService.getCoordinates(city)
                .orElse(new double[] { -33.8688, 151.2093 });

        Temp temp = new Temp();
        temp.setFirstName(faker.name().firstName());
        temp.setLastName(faker.name().lastName());
        temp.setEmail(faker.internet().emailAddress());
        temp.setCity(city);
        temp.setLatitude(coords[0]);
        temp.setLongitude(coords[1]);
        temp.setNotes(faker.lorem().sentence());
        temp.setSkills(skills.stream()
                .limit(2 + faker.random().nextInt(2))
                .toList());

        return temp;
    }
}