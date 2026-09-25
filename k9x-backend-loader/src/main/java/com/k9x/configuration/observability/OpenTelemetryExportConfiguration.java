package com.k9x.configuration.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Switches trace and log export on and off at runtime with {@code k9x-backend.observability.otlp-export-enabled}
 * (OTLP_EXPORT_ENABLED).
 *
 * <p>Spring Boot creates the OTLP exporters behind {@code @ConditionalOn...} checks, and the native image evaluates
 * those once, at build time, where they are forced on (see the processAot task in build.gradle.kts). Every
 * environment therefore carries the exporters, and these beans are what keeps staging from using them: they read
 * the flag when the context starts, not when the image is built. Metrics need nothing of the sort, because the OTLP
 * meter registry checks its own {@code enabled} property again before it starts publishing.
 */
@Configuration
public class OpenTelemetryExportConfiguration {

    /**
     * Every trace, or none: with export off nothing is sampled, so no span ever reaches the exporter. Incoming
     * trace context is still honoured either way.
     */
    @Bean
    public Sampler otelSampler(@Value("${k9x-backend.observability.otlp-export-enabled}") boolean exportEnabled) {
        return exportEnabled ? Sampler.parentBased(Sampler.alwaysOn()) : Sampler.alwaysOff();
    }

    /**
     * Hands the {@link OpenTelemetry} instance to the {@code OTEL} Logback appender declared in logback-spring.xml.
     * Until then the appender drops every event, so with export off it is simply never installed.
     */
    @Bean
    public InitializingBean openTelemetryLogbackAppenderInstaller(OpenTelemetry openTelemetry,
            @Value("${k9x-backend.observability.otlp-export-enabled}") boolean exportEnabled) {
        return () -> {
            if (exportEnabled) {
                OpenTelemetryAppender.install(openTelemetry);
            }
        };
    }
}
