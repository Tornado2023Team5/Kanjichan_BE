package com.github.tornado2023team5.kanjichan.util;

import com.github.tornado2023team5.kanjichan.entity.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RestfulAPIUtil {
    private final RestTemplate restTemplate;

    public <T> T get(String endpoint) {
        var response = restTemplate.exchange(
                endpoint,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<T>() {}
        );
        return response.getBody();
    }

    public <T, R> T post(String endpoint, R request) {
        var response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<T>() {}
        );
        return response.getBody();
    }
}
