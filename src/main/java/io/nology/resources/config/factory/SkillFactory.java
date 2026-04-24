package io.nology.resources.config.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import io.nology.resources.skill.entity.Skill;

@Component
@Profile({ "dev", "test" })
public class SkillFactory {

    private final Faker faker = new Faker();

    private final List<String> skillNames = List.of(
            "Forklift", "Customer Service", "Data Entry",
            "Heavy Lifting", "Cash Handling", "Cleaning",
            "Driving", "Warehousing", "Security",
            "Stock Control", "Packing", "Reception");

    public List<Skill> createSkills(int count) {
        List<Skill> skills = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String name = faker.options().option(skillNames.toArray(new String[0]));
            Skill skill = new Skill();
            skill.setName(name);
            skills.add(skill);
        }

        // deduplicate by name
        return skills.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Skill::getName, s -> s, (a, b) -> a),
                        m -> new ArrayList<>(m.values())));
    }
}