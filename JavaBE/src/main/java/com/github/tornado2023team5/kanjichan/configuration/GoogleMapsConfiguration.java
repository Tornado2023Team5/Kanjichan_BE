package com.github.tornado2023team5.kanjichan.configuration;

import com.google.maps.GeoApiContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleMapsConfiguration {
    private static final String API_KEY = "";

    @Bean
    public GeoApiContext getGeoApiContext() {
        return new GeoApiContext.Builder()
                .apiKey(API_KEY)
                .build();
    }
}
