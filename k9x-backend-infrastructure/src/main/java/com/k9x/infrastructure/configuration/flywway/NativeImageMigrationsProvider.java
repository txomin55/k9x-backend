package com.k9x.infrastructure.configuration.flywway;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;
import org.springframework.core.NativeDetector;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Lists the migrations of a native image, where Flyway's own classpath scanner finds nothing: it does not
 * understand the image's {@code resource:} URLs ("unsupported protocol: resource"), while Spring's resolver
 * does. The same job Spring Boot's Flyway auto-configuration does, which this application does not use because
 * it runs two Flyway instances (schema and data). The files themselves are kept in the image by
 * NativeImageHintsConfiguration.
 */
final class NativeImageMigrationsProvider implements ResourceProvider {

    private final List<Location> locations;
    private final ClassLoader classLoader;
    private final Charset encoding;

    private NativeImageMigrationsProvider(FluentConfiguration configuration) {
        this.locations = Arrays.stream(configuration.getLocations()).filter(Location::isClassPath).toList();
        this.classLoader = configuration.getClassLoader();
        this.encoding = configuration.getEncoding();
    }

    /**
     * Installs the provider on {@code configuration} when running as a native image; on the JVM Flyway keeps
     * scanning the classpath itself.
     */
    static FluentConfiguration applyTo(FluentConfiguration configuration) {
        return NativeDetector.inNativeImage()
                ? configuration.resourceProvider(new NativeImageMigrationsProvider(configuration))
                : configuration;
    }

    @Override
    public LoadableResource getResource(String name) {
        return classLoader.getResource(name) == null ? null : new ClassPathResource(null, name, classLoader, encoding);
    }

    @Override
    public Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        return locations.stream()
                .flatMap(location -> Arrays.stream(list(resolver, location))
                        .map(Resource::getFilename)
                        .filter(Objects::nonNull)
                        .filter(name -> name.startsWith(prefix) && Arrays.stream(suffixes).anyMatch(name::endsWith))
                        .map(name -> (LoadableResource) new ClassPathResource(
                                location, location.getRootPath() + "/" + name, classLoader, encoding)))
                .toList();
    }

    private static Resource[] list(PathMatchingResourcePatternResolver resolver, Location location) {
        try {
            return resolver.getResources("classpath:" + location.getRootPath() + "/*");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list migrations in " + location.getDescriptor(), e);
        }
    }
}
