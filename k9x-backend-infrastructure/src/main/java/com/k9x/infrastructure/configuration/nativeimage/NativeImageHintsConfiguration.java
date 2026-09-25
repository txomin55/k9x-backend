package com.k9x.infrastructure.configuration.nativeimage;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.api.client.util.GenericData;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.infrastructure.out.json.disciplines.obdx.dto.FederationConfigurationFileDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.DefaultCatalog;
import jakarta.servlet.http.HttpServletRequest;
import org.jooq.DataType;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;
import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * What the native image cannot find on its own because it is only reached at runtime by name or by reflection:
 * classpath resources read by path, the DTOs Jackson reads and writes, the records jOOQ and the classes Caffeine
 * instantiate, and Google's token types. Only used when building the native image; the JVM ignores it.
 */
@Configuration
@ImportRuntimeHints(NativeImageHintsConfiguration.Hints.class)
@RegisterReflectionForBinding({FederationConfigurationFileDTO.class, FetchObdxClassificationDTO.class})
public class NativeImageHintsConfiguration {

    static class Hints implements RuntimeHintsRegistrar {

        private static final String OAS_MODEL_PACKAGE = "com.k9x.oas.stub.model";

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources()
                    .registerPattern("com/k9x/infrastructure/out/json/disciplines/obdx/federations/**/configuration.json")
                    .registerPattern("db/schema/*.sql")
                    .registerPattern("db/data/*.sql")
                    .registerPattern("fonts/*.ttf")
                    .registerPattern("i18n/messages*.properties")
                    // OpenPDF's own font metrics and messages, read as resources while it initialises.
                    .registerPattern("org/openpdf/text/pdf/fonts/*")
                    .registerPattern("org/openpdf/text/error_messages/*")
                    .registerPattern("org/openpdf/text/version.properties")
                    .registerPattern("font-fallback/*");

            // The ID token verifier parses Google's JWTs into these through their @Key-annotated fields.
            for (Class<?> type : new Class<?>[]{GoogleIdToken.Payload.class, JsonWebToken.Payload.class,
                    JsonWebToken.Header.class, JsonWebSignature.Header.class, GenericJson.class, GenericData.class}) {
                hints.reflection().registerType(type, MemberCategory.values());
            }

            // jOOQ instantiates the generated record of each table reflectively (selectFrom, newRecord, ...). Walking the
            // generated catalog keeps this in step with the schema.
            for (Schema schema : DefaultCatalog.DEFAULT_CATALOG.getSchemas()) {
                for (Table<?> table : schema.getTables()) {
                    hints.reflection().registerType(table.getRecordType(), MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
                }
            }
            // jOOQ builds the array variant of every data type it knows while initialising DefaultDSLContext. The
            // reachability metadata for jOOQ declares those arrays only once SQLDataType has been initialised, which
            // happens later, and leaves out byte[][] altogether; declare them all unconditionally.
            for (Field field : SQLDataType.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && DataType.class.isAssignableFrom(field.getType())) {
                    try {
                        hints.reflection().registerType(((DataType<?>) field.get(null)).getType().arrayType());
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }

            // The request and response DTOs generated from the OAS. Spring derives hints from each endpoint's signature,
            // but not for the elements of a ResponseEntity<List<...>>, which Jackson then wrote as empty objects.
            BindingReflectionHintsRegistrar binding = new BindingReflectionHintsRegistrar();
            for (Class<?> model : classesIn(OAS_MODEL_PACKAGE, classLoader)) {
                binding.registerReflectionHints(hints.reflection(), model);
            }

            // Refresh takes the current request as a constructor argument, which Spring hands over as a JDK proxy.
            hints.proxies().registerJdkProxy(HttpServletRequest.class);

            // Caffeine picks a generated cache and entry class by name from the builder's settings. Every cache here
            // is maximumSize + expireAfterWrite (SSMSW), one of them with a removal listener (SSLMSW), and their
            // entries are PSWMS; the rest are the superclasses whose fields it reaches through VarHandles. A cache
            // built with other settings (weak keys, refreshAfterWrite, ...) needs its classes added here.
            for (String name : new String[]{"SS", "SSMS", "SSMSW", "SSL", "SSLMS", "SSLMSW", "PS", "PSW", "PSWMS"}) {
                hints.reflection().registerTypeIfPresent(classLoader, "com.github.benmanes.caffeine.cache." + name,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.ACCESS_DECLARED_FIELDS);
            }
        }

        private static List<Class<?>> classesIn(String packageName, ClassLoader classLoader) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
            CachingMetadataReaderFactory readers = new CachingMetadataReaderFactory(resolver);
            try {
                List<Class<?>> classes = new ArrayList<>();
                for (Resource resource : resolver.getResources(
                        "classpath*:" + ClassUtils.convertClassNameToResourcePath(packageName) + "/*.class")) {
                    classes.add(ClassUtils.forName(
                            readers.getMetadataReader(resource).getClassMetadata().getClassName(), classLoader));
                }
                return classes;
            } catch (IOException | ClassNotFoundException e) {
                throw new IllegalStateException("Failed to list the classes of " + packageName, e);
            }
        }
    }
}
