package io.nology.resources.config.factory;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import io.nology.resources.common.services.LocationService;

@Component
@Profile({ "dev", "test" })
public class LocationFactory {

    private final Faker faker = new Faker();
    private final LocationService locationService;

    private final List<String> cities = List.of(
            "Melbourne", "Sydney", "Brisbane", "Perth", "Adelaide");

    public LocationFactory(LocationService locationService) {
        this.locationService = locationService;
    }

    public CityData randomCity() {
        String city = faker.options().option(cities.toArray(new String[0]));

        double[] coords = locationService.getCoordinates(city)
                .orElse(new double[] { -33.8688, 151.2093 });

        return new CityData(city, coords[0], coords[1]);
    }

    public boolean online() {
        return faker.random().nextInt(100) < 35;
    }

    public record CityData(String city, double lat, double lng) {
    }
}