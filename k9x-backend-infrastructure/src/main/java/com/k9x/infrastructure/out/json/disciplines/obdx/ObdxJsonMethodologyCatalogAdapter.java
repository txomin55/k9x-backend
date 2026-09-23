package com.k9x.infrastructure.out.json.disciplines.obdx;

import com.k9x.application.methodology.port.GetMethodologyCatalogPort;
import com.k9x.application.methodology.use_case.dto.LocalizedTextDTO;
import com.k9x.application.methodology.use_case.dto.MethodologyCatalogDTO;
import com.k9x.domain.disciplines.obdx.ObdxEventCategory;
import com.k9x.infrastructure.out.json.disciplines.obdx.dto.FederationConfigurationFileDTO;
import org.springframework.context.MessageSource;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The names of the methodology pages, read from the configuration files and the message bundles. The pages are
 * bilingual, so every name is resolved in both languages at once rather than in the request's locale.
 */
public class ObdxJsonMethodologyCatalogAdapter implements GetMethodologyCatalogPort {

    private static final Locale SPANISH = Locale.forLanguageTag("es");
    private static final Locale ENGLISH = Locale.ENGLISH;

    private final ObdxFederationsConfigurationsCache cache;
    private final MessageSource messageSource;

    public ObdxJsonMethodologyCatalogAdapter(ObdxFederationsConfigurationsCache cache, MessageSource messageSource) {
        this.cache = cache;
        this.messageSource = messageSource;
    }

    @Override
    public MethodologyCatalogDTO getCatalog() {
        List<ObdxFederationsConfigurationsCache.Entry> current = cache.getCurrent();

        List<MethodologyCatalogDTO.Configuration> configurations = current.stream()
                .map(entry -> new MethodologyCatalogDTO.Configuration(
                        entry.federationKey().toUpperCase(Locale.ROOT),
                        translate("federation." + entry.federationKey() + ".full_name", ENGLISH,
                                entry.federationKey().toUpperCase(Locale.ROOT)),
                        entry.configuration().id(),
                        localized(entry.configuration().id())))
                .toList();

        Map<ObdxEventCategory, LocalizedTextDTO> categoryNames = new EnumMap<>(ObdxEventCategory.class);
        for (ObdxEventCategory category : ObdxEventCategory.values()) {
            categoryNames.put(category, localized("event.category." + category.name().toLowerCase(Locale.ROOT) + ".name"));
        }

        Map<String, String> qualificationEnglishNames = new LinkedHashMap<>();
        current.stream()
                .flatMap(entry -> entry.configuration().qualifications() == null
                        ? Stream.empty()
                        : entry.configuration().qualifications().stream())
                .map(FederationConfigurationFileDTO.Qualification::id)
                .filter(Objects::nonNull)
                .forEach(id -> qualificationEnglishNames.computeIfAbsent(id, _ -> translate(
                        "obdx.qualification." + id.toLowerCase(Locale.ROOT) + ".short", ENGLISH, id)));

        return new MethodologyCatalogDTO(configurations, categoryNames, qualificationEnglishNames);
    }

    private LocalizedTextDTO localized(String key) {
        return new LocalizedTextDTO(translate(key, SPANISH, key), translate(key, ENGLISH, key));
    }

    private String translate(String key, Locale locale, String defaultValue) {
        return messageSource.getMessage(key, null, defaultValue, locale);
    }
}
