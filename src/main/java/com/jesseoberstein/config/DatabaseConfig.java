package com.jesseoberstein.config;

import com.github.cloudyrock.spring.v5.EnableMongock;
import com.jesseoberstein.converters.ZonedDateTimeReadConverter;
import com.jesseoberstein.converters.ZonedDateTimeWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.List;

@Configuration
@EnableMongock
@EnableMongoRepositories(basePackages = "com.jesseoberstein.dao")
public class DatabaseConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        var conversions= List.of(
            new ZonedDateTimeReadConverter(),
            new ZonedDateTimeWriteConverter());

        return new MongoCustomConversions(conversions);
    }
}
