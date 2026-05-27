package io.nology.resources.recommendation;

import java.math.BigDecimal;
import java.util.List;

public record TempRecommendation(
                Long tempId,
                String firstName,
                String lastName,
                String city,
                BigDecimal rating,
                List<String> matchedSkills,
                List<String> missingSkills,
                int skillMatchPercent,
                Double distanceKm,
                int rank,
                String reasoning) {
}
