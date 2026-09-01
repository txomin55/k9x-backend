package com.k9x.infrastructure.out.enums.breeds;

import com.k9x.application.breeds.port.GetBreedListPort;
import com.k9x.application.breeds.use_case.dto.BreedDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BreedEnumAdapter implements GetBreedListPort {

    private final MessageSource messageSource;

    public BreedEnumAdapter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public List<BreedDTO> getBreeds() {
        // Sorted by the translated name, with the locale's own alphabet rules (accents, ñ, ...).
        Collator collator = Collator.getInstance(LocaleContextHolder.getLocale());
        return Arrays.stream(Breed.values())
                .map(breed -> new BreedDTO(breed.name(), translate(breed)))
                .sorted(Comparator.comparing(BreedDTO::name, collator))
                .toList();
    }

    private String translate(Breed breed) {
        String key = "breed." + breed.name().toLowerCase() + ".name";
        return messageSource.getMessage(key, null, breed.name(), LocaleContextHolder.getLocale());
    }
}
