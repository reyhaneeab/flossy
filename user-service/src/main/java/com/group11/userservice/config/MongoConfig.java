package com.group11.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
public class MongoConfig {
    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoMappingContext context, MongoCustomConversions conversions) {
        // Create a new MappingMongoConverter with NoOpDbRefResolver and the provided context
        MappingMongoConverter mappingConverter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
        
        // Set custom conversions for the converter
        mappingConverter.setCustomConversions(conversions);
        
        // Disable the default type mapping (removes '_class' field from MongoDB documents)
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        
        // Return the configured converter
        return mappingConverter;
    }
}