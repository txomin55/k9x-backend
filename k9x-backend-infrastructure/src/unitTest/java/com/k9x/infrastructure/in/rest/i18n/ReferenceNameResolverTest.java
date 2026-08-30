package com.k9x.infrastructure.in.rest.i18n;

import com.k9x.domain.competitions.aggregates.CompetitionExtraction;
import com.k9x.oas.stub.model.ExtractionResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against the real bundle: the hint is the only place where the stored extraction type becomes a sentence,
 * so a missing or mis-keyed translation must fail here and not in the browser.
 */
class ReferenceNameResolverTest {

    private static ReferenceNameResolver resolver() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return new ReferenceNameResolver((MessageSource) messageSource);
    }

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void projects_nothing_when_the_competition_was_not_extracted() {
        assertThat(resolver().extraction(null)).isNull();
    }

    @Test
    void translates_the_type_and_its_parameter_into_a_hint() {
        LocaleContextHolder.setLocale(Locale.of("es"));

        ExtractionResponseDTO dto = resolver().extraction(
                new CompetitionExtraction("cpc-2020-9-extraction", "https://cpc/2020/9", 1000L, "FEDERATION_PAGE,cpc"));

        assertThat(dto.getExtractionId()).isEqualTo("cpc-2020-9-extraction");
        assertThat(dto.getSource().getUrl()).isEqualTo("https://cpc/2020/9");
        assertThat(dto.getSource().getExtractionTimestamp()).isEqualTo(1000L);
        assertThat(dto.getHint()).isEqualTo("Resultados publicados en la página de la federación Cpc");
    }

    @Test
    void translates_the_parameter_of_a_type_that_is_not_a_federation() {
        LocaleContextHolder.setLocale(Locale.of("es"));

        ExtractionResponseDTO dto = resolver().extraction(
                new CompetitionExtraction("id", null, 1L, "PRIVATE_CONVERSATIONS,ORGANIZER"));

        assertThat(dto.getHint()).isEqualTo("Resultados facilitados por el organizador en conversaciones privadas");
    }

    @Test
    void leaves_no_dangling_space_when_the_type_carries_no_parameters() {
        LocaleContextHolder.setLocale(Locale.of("es"));

        ExtractionResponseDTO dto = resolver().extraction(new CompetitionExtraction("id", null, 1L, "FEDERATION_PAGE"));

        assertThat(dto.getHint()).isEqualTo("Resultados publicados en la página de la federación");
    }

    @Test
    void falls_back_to_the_generic_hint_when_the_type_is_unknown_or_missing() {
        LocaleContextHolder.setLocale(Locale.of("es"));
        ReferenceNameResolver resolver = resolver();

        assertThat(resolver.extraction(new CompetitionExtraction("id", null, 1L, "SOMETHING_ELSE")).getHint())
                .isEqualTo("Resultados recogidos fuera de k9x");
        assertThat(resolver.extraction(CompetitionExtraction.UNKNOWN).getHint())
                .isEqualTo("Resultados recogidos fuera de k9x");
    }
}
