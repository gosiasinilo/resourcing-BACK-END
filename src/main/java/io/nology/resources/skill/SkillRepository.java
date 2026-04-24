package io.nology.resources.skill;

import org.springframework.data.jpa.repository.JpaRepository;

import io.nology.resources.skill.entity.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}