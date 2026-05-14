package com.k9x.infrastructure.out.mongo.configuration;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import java.util.Collections;

@NullMarked
@Configuration
public class MongoClient extends AbstractMongoClientConfiguration {

    @Value("${spring.mongodb.authentication-database}")
    private String authenticationDatabase;

    @Value("${spring.mongodb.host}")
    private String mongoHost;

    @Value("${spring.mongodb.port}")
    private Integer mongoPort;

    @Value("${spring.mongodb.database}")
    private String mongoDatabaseName;

    @Value("${spring.mongodb.username}")
    private String username;

    @Value("${spring.mongodb.password}")
    private String password;

    @Override
    public com.mongodb.client.MongoClient mongoClient() {
        return MongoClients.create(
                MongoClientSettings.builder()
                        .applicationName(mongoDatabaseName)
                        .credential(MongoCredential.createCredential(
                                username,
                                authenticationDatabase,
                                password.toCharArray()
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
