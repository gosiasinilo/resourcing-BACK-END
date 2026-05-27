package io.nology.resources.common.services;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private static final Map<String, double[]> AU_CITIES = Map.of(
        "melbourne", new double[]{-37.8136, 144.9631},
        "sydney",    new double[]{-33.8688, 151.2093},
        "brisbane",  new double[]{-27.4698, 153.0251},
        "perth",     new double[]{-31.9505, 115.8605},
        "adelaide",  new double[]{-34.9285, 138.6007},
        "canberra",  new double[]{-35.2809, 149.1300},
        "hobart",    new double[]{-42.8821, 147.3272},
        "darwin",    new double[]{-12.4634, 130.8456}
    );

    public Optional<double[]> getCoordinates(String city) {
        if (city == null || city.isBlank()) return Optional.empty();
        double[] coords = AU_CITIES.get(city.trim().toLowerCase());
        return Optional.ofNullable(coords);
    }
}
