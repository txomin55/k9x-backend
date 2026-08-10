package com.k9x.infrastructure.in.rest.i18n;

import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * Resolves the localised name of the reference values that travel through the application layer as bare ids
 * (breeds, countries, disciplines). Every read endpoint and the xlsx export share this resolver so the same id
 * always renders the same name and the message key format lives in a single place.
 *
 * <p>An unknown id falls back to the id itself rather than failing the whole response: a dog whose breed is not
 * in the bundle yet should still be listed.
 */
public class ReferenceNameResolver {

    private final MessageSource messageSource;

    public ReferenceNameResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /** Breed keys are the lowercased enum constant: {@code breed.border_collie.name}. */
    public String breedName(String breedId) {
        return breedId == null ? null : translate("breed." + breedId.toLowerCase(Locale.ROOT) + ".name", breedId);
    }

    public IdNameDTO breed(String breedId) {
        return breedId == null ? null : new IdNameDTO(breedName(breedId), breedId);
    }

    /** Country keys are the lowercased ISO code: {@code country.es.name}. */
    public String countryName(String countryCode) {
        return countryCode == null ? null
                : translate("country." + countryCode.toLowerCase(Locale.ROOT) + ".name", countryCode);
    }

    public IdNameDTO country(String countryCode) {
        return countryCode == null ? null : new IdNameDTO(countryName(countryCode), countryCode);
    }

    /** Discipline keys keep the uppercased id: {@code discipline.OBDX.name}. */
    public String disciplineName(String disciplineId) {
        return disciplineId == null ? null
                : translate("discipline." + disciplineId.toUpperCase(Locale.ROOT) + ".name", disciplineId);
    }

    public IdNameDTO discipline(String disciplineId) {
        return disciplineId == null ? null : new IdNameDTO(disciplineName(disciplineId), disciplineId);
    }

    private String translate(String key, String fallback) {
        return messageSource.getMessage(key, null, fallback, LocaleContextHolder.getLocale());
    }
}
