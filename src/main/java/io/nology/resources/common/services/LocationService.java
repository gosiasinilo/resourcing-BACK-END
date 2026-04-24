package io.nology.resources.common.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Service
public class LocationService {

    private final RestClient restClient;

    public LocationService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", "resources-app")
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimResult(String lat, String lon) {
    }

    public Optional<double[]> getCoordinates(String city) {
        try {
            List<NominatimResult> results = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", city)
                            .queryParam("countrycode", "au")
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<NominatimResult>>() {
                    });

            if (results == null || results.isEmpty()) {
                return Optional.empty();
            }

            double lat = Double.parseDouble(results.get(0).lat());
            double lon = Double.parseDouble(results.get(0).lon());
            return Optional.of(new double[] { lat, lon });

        } catch (Exception e) {
            return Optional.empty();
        }
    }
}