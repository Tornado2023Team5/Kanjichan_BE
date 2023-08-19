package com.github.tornado2023team5.kanjichan.service;

import com.github.tornado2023team5.kanjichan.model.ShopCategory;
import com.github.tornado2023team5.kanjichan.model.ShopInfo;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoogleMapsService {
    private final GeoApiContext context;

    public PlacesSearchResult[] getShopInfo(String location, ShopCategory category) throws IOException, InterruptedException, ApiException {
        var response = PlacesApi.textSearchQuery(context, location + " " + category.getValue())
                .type(PlaceType.RESTAURANT)
                .await();
        return response.results;
    }

    public HashMap<String, URL> getWebSite(PlacesSearchResult[] results) throws IOException, InterruptedException, ApiException {
        return new HashMap<>(){{
            for (PlacesSearchResult result : results) {
                var request = PlacesApi.placeDetails(context, result.placeId);
                request.fields(Arrays.stream(PlaceDetailsRequest.FieldMask.values())
                        .filter(x -> x != PlaceDetailsRequest.FieldMask.SECONDARY_OPENING_HOURS)
                        .toArray(PlaceDetailsRequest.FieldMask[]::new));
                put(result.name, request.await().website);
            }
        }};
    }
}
