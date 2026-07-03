package com.k9x.infrastructure.out.enums.breeds;

import com.k9x.application.breeds.port.GetBreedListPort;
import com.k9x.application.breeds.use_case.dto.BreedDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Arrays;
import java.util.List;

public class BreedEnumAdapter implements GetBreedListPort {

    private final MessageSource messageSource;

    public BreedEnumAdapter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public List<BreedDTO> getBreeds() {
        return Arrays.stream(Breed.values())
                .map(breed -> new BreedDTO(breed.name(), translate(breed)))
                .toList();
    }

    private String translate(Breed breed) {
        String key = "breed." + breed.name().toLowerCase() + ".name";
        return messageSource.getMessage(key, null, breed.name(), LocaleContextHolder.getLocale());
    }
}
