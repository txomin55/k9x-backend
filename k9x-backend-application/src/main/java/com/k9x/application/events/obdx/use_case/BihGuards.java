package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.exceptions.BihNotAllowedForSexException;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;

public final class BihGuards {

    private BihGuards() {}

    public static void assertBihAllowedForSex(boolean bih, Dog dog) {
        if (bih && dog.getSex() != Sex.FEMALE) {
            throw new BihNotAllowedForSexException();
        }
    }
}
