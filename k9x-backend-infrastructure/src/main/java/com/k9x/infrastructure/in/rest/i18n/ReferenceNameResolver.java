package com.k9x.infrastructure.in.rest.i18n;

import com.k9x.domain.competitions.aggregates.CompetitionExtraction;
import com.k9x.oas.stub.model.ExtractionResponseDTO;
import com.k9x.oas.stub.model.ExtractionSourceResponseDTO;
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

    /**
     * Projects the provenance of an extracted competition, translating its raw {@code type} into a sentence the
     * reader can act on: {@code FEDERATION_PAGE,cpc} becomes the hint for {@code extraction.type.federation_page}
     * with {@code cpc} — itself translated — as its parameter. An unknown type falls back to the raw value rather
     * than hiding the warning.
     */
    public ExtractionResponseDTO extraction(CompetitionExtraction extraction) {
        if (extraction == null) {
            return null;
        }
        return new ExtractionResponseDTO(
                extraction.extractionId(),
                new ExtractionSourceResponseDTO(extraction.url(), extraction.extractionTimestamp()),
                extractionHint(extraction));
    }

    private String extractionHint(CompetitionExtraction extraction) {
        String type = extraction.typeToken();
        if (type == null) {
            return translate("extraction.type.unknown.hint", null, null);
        }
        // A type with no parameters leaves its placeholder empty, hence the trim: 'FEDERATION_PAGE' alone must
        // not render a dangling space.
        Object[] params = extraction.typeParams().isEmpty()
                ? new Object[]{""}
                : extraction.typeParams().stream().map(this::extractionParamName).toArray();
        String hint = translate("extraction.type." + type.toLowerCase(Locale.ROOT) + ".hint", params,
                translate("extraction.type.unknown.hint", null, type));
        return hint == null ? null : hint.trim();
    }

    /** Parameters are federations more often than not, so their existing names are reused before falling back. */
    private String extractionParamName(String param) {
        String key = "extraction.param." + param.toLowerCase(Locale.ROOT);
        return translate(key, null, translate("federation." + param.toLowerCase(Locale.ROOT) + ".name", param));
    }

    private String translate(String key, Object[] args, String fallback) {
        return messageSource.getMessage(key, args, fallback, LocaleContextHolder.getLocale());
    }

    private String translate(String key, String fallback) {
        return messageSource.getMessage(key, null, fallback, LocaleContextHolder.getLocale());
    }
}
