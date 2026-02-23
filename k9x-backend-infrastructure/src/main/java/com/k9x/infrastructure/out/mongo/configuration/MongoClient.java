package com.k9x.infrastructure.out.mongo.configuration;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import java.util.Collections;
import java.util.Optional;

@NullMarked
@Configuration
@ConditionalOnProperty(value = "k9x-backend.deploy.tech", havingValue = "mongo")
public class MongoClient extends AbstractMongoClientConfiguration {

    @Value("${spring.mongodb.authentication-database}")
    private Optional<String> authenticationDatabase;

    @Value("${spring.mongodb.host}")
    private String mongoHost;

    @Value("${spring.mongodb.port}")
    private Integer mongoPort;

    @Value("${spring.mongodb.database}")
    private String mongoDatabaseName;

    @Value("${spring.mongodb.username}")
    private Optional<String> username;

    @Value("${spring.mongodb.password}")
    private Optional<String> password;

    @Override
    public com.mongodb.client.MongoClient mongoClient() {
        String user = username.orElseThrow(() ->
                new IllegalStateException("Mongo username is required when authenticationDatabase is set"));
        String pass = password.orElseThrow(() ->
                new IllegalStateException("Mongo password is required when authenticationDatabase is set"));
        return MongoClients.create(
                MongoClientSettings.builder()
                        .applicationName(mongoDatabaseName)
                        .credential(MongoCredential.createCredential(
                                user,
                                authenticationDatabase.get(),
                                pass.toCharArray()
                        ))
                        .applyToClusterSettings(builder ->
                                builder.hosts(Collections.singletonList(new ServerAddress(mongoHost, mongoPort))))
                        .build());
    }

    @Override
    public String getDatabaseName() {
        return mongoDatabaseName;
    }
}
