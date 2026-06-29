package com.k9x.infrastructure.configuration.flywway;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class FlywayConfiguration {

    @Primary
    @Bean(initMethod = "migrate")
    public Flyway schemaFlyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/schema")
                .table("flyway_schema_history")
                .load();
    }

    @Bean(initMethod = "migrate")
    @DependsOn("schemaFlyway")
    public Flyway dataFlyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/data")
                .table("flyway_data_history")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
    }
}
