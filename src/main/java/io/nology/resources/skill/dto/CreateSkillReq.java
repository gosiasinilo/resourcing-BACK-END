package io.nology.resources.skill.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSkillReq(
        @NotBlank(message = "Skill name is required") String name) {
}