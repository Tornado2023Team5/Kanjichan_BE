package com.github.tornado2023team5.kanjichan.service;

import com.github.tornado2023team5.kanjichan.entity.Action;
import com.github.tornado2023team5.kanjichan.model.Address;
import com.github.tornado2023team5.kanjichan.model.function.ShopCategory;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import kotlin.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoogleMapsService {
    private final GeoApiContext context;

    private PlacesSearchResult[] getShopResult(String location, ShopCategory category) throws IOException, InterruptedException, ApiException {
        var response = PlacesApi.textSearchQuery(context, location + " " + category.getValue()).language("ja").await();
        return response.results;
    }

    public PlacesSearchResult getStation(String location) throws IOException, InterruptedException, ApiException {
        var response = PlacesApi.textSearchQuery(context, location + " 最寄り駅").language("ja").await();
        return response.results[0];
    }

    private HashMap<PlacesSearchResult, PlaceDetails> getPlaceDetails(PlacesSearchResult[] results) throws IOException, InterruptedException, ApiException {
        return new HashMap<>() {{
            for (var result : results) {
                var request = PlacesApi.placeDetails(context, result.placeId).language("ja");
                request.fields(Arrays.stream(PlaceDetailsRequest.FieldMask.values())
                                       .filter(x -> x != PlaceDetailsRequest.FieldMask.SECONDARY_OPENING_HOURS)
                                       .toArray(PlaceDetailsRequest.FieldMask[]::new));
                put(result, request.await());
            }
        }};
    }

    public List<PlaceDetails> getShopInfo(String location, ShopCategory category) throws IOException, InterruptedException, ApiException {
        var results = getShopResult(location, category);
        var details = getPlaceDetails(Arrays.stream(results).limit(5).toArray(PlacesSearchResult[]::new));
        return details.entrySet().stream()
                .filter(entry -> entry.getValue().rating >= 3.0)
                .limit(3)
                .map(Map.Entry::getValue)
                .toList();
    }

    public static String getRatingStars(double rating) {
        int numStars = (int) Math.round(rating);
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= numStars) {
                stars.append("★"); // 星（★）を追加
            } else {
                stars.append("☆"); // 星じゃない（☆）を追加
            }
        }
        return stars.toString();
    }

    public Address getAddressDetails(String addressString) {
        GeocodingResult[] results;
        try {
            results = GeocodingApi.geocode(context, addressString).await();
        } catch (ApiException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        Address address = new Address();
        if (results.length > 0) {
            address.setAddress(results[0].formattedAddress);
            address.setLatitude(results[0].geometry.location.lat);
            address.setLongitude(results[0].geometry.location.lng);
        }

        return address;
    }

    private static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371; // 地球の半径 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static double[][] fullSearch(List<Address> addresses) {
        double[][] distances = new double[addresses.size()][addresses.size()];
        for (int i = 0; i < addresses.size(); i++) {
            for (int j = i + 1; j < addresses.size(); j++) {
                double distance = haversineDistance(addresses.get(i).getLatitude(), addresses.get(i).getLongitude(), addresses.get(j).getLatitude(), addresses.get(j).getLongitude());
                distances[i][j] = distances[j][i] = distance;
            }
        }
        return distances;
    }

    // 最初の地点からその点まで戻る一筆書きの最適経路を求める
    public List<Action> sortByDistance(List<Action> draft, String start) throws IOException, InterruptedException, ApiException {
        var response = PlacesApi.textSearchQuery(context, start).language("ja").await();
        var station = Arrays.stream(response.results).findFirst();
        if (station.isEmpty()) return new ArrayList<>();
        var stationAction = new Action();
        stationAction.setLocation(station.get().formattedAddress);
        stationAction.setName(station.get().name);
        draft.add(0, stationAction);
        var addresses = draft.stream().map(action -> getAddressDetails(action.getLocation())).toList();

        double[][] distances = fullSearch(addresses);

        var passed = new ArrayDeque<Integer>();
        passed.addLast(0);
        var rem = new HashSet<Integer>() {{
            for (int i = 1; i < addresses.size(); i++) add(i);
        }};
        var result = DFS(passed, rem, 0, distances);
        return new ArrayList<>() {{
            for (int i : result.getSecond()) add(draft.get(i));
        }};
    }

    private static Pair<Double, List<Integer>> DFS(Deque<Integer> passed, Set<Integer> rem, double distance, double[][] distances) {
        int current = passed.getLast();
        rem.remove(current);

        if (rem.size() == 0) {
            int first = passed.getFirst();
            List<Integer> route = new ArrayList<>(passed.stream().toList());
            route.add(first);
            return new Pair<>(distance + distances[current][first], route);
        }

        Pair<Double, List<Integer>> min = new Pair<>(Double.MAX_VALUE, null);

        for (int next : rem) {
            passed.addLast(next);
            var shortest = DFS(passed, rem, distance + distances[current][next], distances);
            if (min.getSecond() == null || shortest.getFirst() < min.getFirst()) min = shortest;
            passed.removeLast();
        }

        return min;
    }
}
