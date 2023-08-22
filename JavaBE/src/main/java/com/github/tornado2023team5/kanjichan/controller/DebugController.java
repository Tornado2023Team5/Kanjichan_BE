package com.github.tornado2023team5.kanjichan.controller;

import com.github.tornado2023team5.kanjichan.service.GoogleMapsService;
import com.github.tornado2023team5.kanjichan.service.PickUpCategoryService;
import com.google.maps.errors.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DebugController {
    private final PickUpCategoryService service;
    private final GoogleMapsService googleMapsService;

    @PostMapping("/public/debug")
    public String debugPost(@RequestParam String input) {
        return service.pickUp(input).toString();
    }

    @GetMapping("/public/debug")
    public String debugGet(@RequestParam String input) {
        return service.pickUp(input).toString();
    }

    @GetMapping("/public/debug2")
    public List<String> debug2Get(@RequestParam String input, @RequestParam String location) throws IOException, InterruptedException, ApiException {
        var category = service.pickUp(input);
        return Arrays.stream(googleMapsService.getShopInfo(location, category)).map((place) -> place.name).toList();
    }

    @GetMapping("/public/debug3")
    public Map debug3Get(@RequestParam String input, @RequestParam String location) throws IOException, InterruptedException, ApiException {
        var category = service.pickUp(input);
        return googleMapsService.getWebSites(googleMapsService.getShopInfo(location, category));
    }

}
