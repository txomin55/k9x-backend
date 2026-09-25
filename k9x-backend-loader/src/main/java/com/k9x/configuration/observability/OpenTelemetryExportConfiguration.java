package com.k9x.configuration.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.otlp.OtlpTracingProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

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
     * The span exporter, declared here instead of left to Spring Boot, which picks HTTP or gRPC with a
     * {@code @ConditionalOnProperty} on the transport. The staging image built by CI on 2026-09-25 came out with the
     * gRPC exporter although nothing sets the transport, while the same build done locally chose HTTP, and the gRPC
     * one refuses to start against the HTTP transport it then reads. With this bean both of Boot's exporters back
     * off, and the choice no longer depends on how the AOT build evaluates that condition. It is built exactly as
     * Boot's HTTP one, from the same {@code management.opentelemetry.tracing.export.otlp} properties.
     */
    @Bean
    public OtlpHttpSpanExporter otlpHttpSpanExporter(OtlpTracingProperties properties,
            ObjectProvider<MeterProvider> meterProvider) {
        OtlpHttpSpanExporterBuilder builder = OtlpHttpSpanExporter.builder()
                .setTimeout(properties.getTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setCompression(properties.getCompression().name().toLowerCase(Locale.ROOT));
        if (properties.getEndpoint() != null) {
            builder.setEndpoint(properties.getEndpoint());
        }
        properties.getHeaders().forEach(builder::addHeader);
        meterProvider.ifAvailable(builder::setMeterProvider);
        return builder.build();
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
