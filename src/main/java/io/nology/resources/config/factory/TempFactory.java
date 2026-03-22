package io.nology.resources.config.factory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import io.nology.resources.temp.entity.Temp;

@Component
@Profile({ "dev", "test" })
public class TempFactory {

    private final Faker faker = new Faker();

    public Temp createTemp() {
        Temp temp = new Temp();
        temp.setFirstName(faker.name().firstName());
        temp.setLastName(faker.name().lastName());
        return temp;
    }
}