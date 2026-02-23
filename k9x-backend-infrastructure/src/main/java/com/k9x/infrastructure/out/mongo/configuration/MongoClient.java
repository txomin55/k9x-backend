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

    @Value("${spring.mongodb.authentication-database:#{null}}")
    private Optional<String> authenticationDatabase;

    @Value("${spring.mongodb.host}")
    private String mongoHost;

    @Value("${spring.mongodb.port}")
    private Integer mongoPort;

    @Value("${spring.mongodb.database}")
    private String mongoDatabaseName;

    @Value("${spring.mongodb.username:#{null}}")
    private Optional<String> username;

    @Value("${spring.mongodb.password:#{null}}")
    private Optional<String> password;

    @Override
    public com.mongodb.client.MongoClient mongoClient() {
        if (authenticationDatabase.isPresent()) {
            return MongoClients.create(
                    MongoClientSettings.builder()
                            .applicationName(mongoDatabaseName)
                            .credential(MongoCredential.createCredential(username.toString(), authenticationDatabase.toString(),
                                    password.toString().toCharArray()))
                            .applyToClusterSettings(builder ->
                                    builder.hosts(Collections.singletonList(new ServerAddress(mongoHost, mongoPort))))
                            .build());
        }

        return MongoClients.create(
                MongoClientSettings.builder()
                        .applicationName(mongoDatabaseName)
                        .applyToClusterSettings(builder ->
                                builder.hosts(Collections.singletonList(new ServerAddress(mongoHost, mongoPort))))
                        .build());
    }

    @Override
    public String getDatabaseName() {
        return mongoDatabaseName;
    }
}
