package io.nology.resources.recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nology.resources.common.exception.NotFoundException;
import io.nology.resources.common.serviceErrors.NotFoundError;
import io.nology.resources.job.JobRepository;
import io.nology.resources.job.entity.Job;
import io.nology.resources.job.service.TempAvailabilityService;
import io.nology.resources.temp.TempRepository;
import io.nology.resources.temp.entity.Temp;

@Service
public class RecommendationService {

    private final JobRepository jobRepository;
    private final TempRepository tempRepository;
    private final TempAvailabilityService tempAvailability;
    private final RestClient anthropicClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${anthropic.api.key:}")
    private String apiKey;

    public RecommendationService(
            JobRepository jobRepository,
            TempRepository tempRepository,
            TempAvailabilityService tempAvailability) {
        this.jobRepository = jobRepository;
        this.tempRepository = tempRepository;
        this.tempAvailability = tempAvailability;
        this.anthropicClient = RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .build();
    }

    public List<TempRecommendation> recommend(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException(new NotFoundError("Job", jobId)));

        List<String> requiredSkills = job.getRequiredSkills().stream()
                .map(s -> s.getName())
                .toList();

        List<Temp> available = tempRepository.findAll().stream()
                .filter(t -> tempAvailability.isTempAvailable(t, job.getStartDate(), job.getEndDate()))
                .toList();

        if (available.isEmpty()) return List.of();

        List<CandidateScore> scored = available.stream().map(temp -> {
            List<String> tempSkills = temp.getSkills().stream().map(s -> s.getName()).toList();
            List<String> matched = requiredSkills.stream().filter(tempSkills::contains).toList();
            List<String> missing = requiredSkills.stream().filter(s -> !tempSkills.contains(s)).toList();
            int skillPct = requiredSkills.isEmpty() ? 100
                    : (int) Math.round(100.0 * matched.size() / requiredSkills.size());
            Double distance = null;
            if (job.getJobType() == Job.JobType.LOCATION
                    && job.getLatitude() != null && job.getLongitude() != null
                    && temp.getLatitude() != null && temp.getLongitude() != null) {
                distance = haversineKm(job.getLatitude(), job.getLongitude(),
                        temp.getLatitude(), temp.getLongitude());
            }
            return new CandidateScore(temp, matched, missing, skillPct, distance);
        }).toList();

        String prompt = buildPrompt(job, requiredSkills, scored);
        List<RankedTemp> rankings = callClaude(prompt, scored.size());

        Map<Long, CandidateScore> scoreMap = scored.stream()
                .collect(Collectors.toMap(c -> c.temp().getId(), c -> c));

        List<TempRecommendation> result = new ArrayList<>();
        for (RankedTemp r : rankings) {
            CandidateScore c = scoreMap.get(r.tempId());
            if (c == null) continue;
            result.add(new TempRecommendation(
                    c.temp().getId(), c.temp().getFirstName(), c.temp().getLastName(),
                    c.temp().getCity(), c.temp().getRating(),
                    c.matched(), c.missing(), c.skillPct(), c.distance(),
                    r.rank(), r.reasoning()));
        }
        return result;
    }

    private String buildPrompt(Job job, List<String> requiredSkills, List<CandidateScore> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a workforce matching assistant. Rank the following temporary workers for a job.\n\n");
        sb.append("JOB DETAILS:\n");
        sb.append("- Name: ").append(job.getName()).append("\n");
        if (job.getDescription() != null)
            sb.append("- Description: ").append(job.getDescription()).append("\n");
        sb.append("- Type: ").append(job.getJobType()).append("\n");
        sb.append("- Dates: ").append(job.getStartDate()).append(" to ").append(job.getEndDate()).append("\n");
        if (job.getCity() != null)
            sb.append("- Location: ").append(job.getCity()).append("\n");
        sb.append("- Required skills: ").append(
                requiredSkills.isEmpty() ? "None specified" : String.join(", ", requiredSkills)).append("\n\n");
        sb.append("CANDIDATES:\n");
        for (CandidateScore c : candidates) {
            sb.append("- ID ").append(c.temp().getId())
                    .append(": ").append(c.temp().getFirstName()).append(" ").append(c.temp().getLastName())
                    .append(", ").append(c.temp().getCity())
                    .append(", Rating: ").append(c.temp().getRating() != null ? c.temp().getRating() : "No rating")
                    .append(", Skill match: ").append(c.skillPct()).append("%")
                    .append(", Matched: [").append(String.join(", ", c.matched())).append("]")
                    .append(", Missing: [").append(String.join(", ", c.missing())).append("]");
            if (c.distance() != null)
                sb.append(", Distance: ").append(String.format("%.1f", c.distance())).append("km");
            if (c.temp().getNotes() != null)
                sb.append(", Notes: ").append(c.temp().getNotes());
            sb.append("\n");
        }
        sb.append("\nRank ALL candidates from best to worst fit. ");
        sb.append("Consider: skill match first, then rating, then distance (closer is better for LOCATION jobs). ");
        sb.append("Respond ONLY with a JSON array, no markdown, no explanation outside the array:\n");
        sb.append("[{\"tempId\": 1, \"rank\": 1, \"reasoning\": \"Brief reason\"}, ...]\n");
        sb.append("Every candidate must appear exactly once.");
        return sb.toString();
    }

    private List<RankedTemp> callClaude(String prompt, int expectedCount) {
        if (apiKey == null || apiKey.isBlank()) return List.of();
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", "claude-sonnet-4-20250514",
                    "max_tokens", 1024,
                    "messages", List.of(Map.of("role", "user", "content", prompt))));
            String response = anthropicClient.post()
                    .uri("/v1/messages")
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .body(body)
                    .retrieve()
                    .body(String.class);
            var parsed = objectMapper.readTree(response);
            String text = parsed.at("/content/0/text").asText();
            text = text.replaceAll("```json|```", "").trim();
            var rankings = objectMapper.readTree(text);
            List<RankedTemp> result = new ArrayList<>();
            for (var node : rankings) {
                result.add(new RankedTemp(
                        node.get("tempId").asLong(),
                        node.get("rank").asInt(),
                        node.get("reasoning").asText()));
            }
            return result;
        } catch (Exception e) {
            System.err.println("Claude API call failed: " + e.getMessage());
            return List.of();
        }
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private record CandidateScore(
            Temp temp, List<String> matched, List<String> missing,
            int skillPct, Double distance) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RankedTemp(Long tempId, int rank, String reasoning) {}
}
