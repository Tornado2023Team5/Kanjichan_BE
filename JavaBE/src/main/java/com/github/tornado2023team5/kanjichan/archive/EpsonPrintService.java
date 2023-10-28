package com.github.tornado2023team5.kanjichan.archive;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

//@Service
//@RequiredArgsConstructor
//public class EpsonPrintService {
//    private final RestTemplate restTemplate;
//
//    @Value("${epson.api.endpoint}")
//    private String EPSON_CONNECT_API_ENDPOINT;
//
//    @Value("${epson.client.id}")
//    private String CLIENT_ID;
//
//    @Value("${epson.client.secret}")
//    private String CLIENT_SECRET;
//
//    public void printImage(String printerEmail, String imageUrl) {
//        Map<String, String> accessMap = getAccessToken(printerEmail);
//        Map<String, String> jobMap = createJob(accessMap.get("access_token"), accessMap.get("subject_id"), "Moon_Shiori");
//        uploadImage(accessMap.get("access_token"), jobMap.get("upload_uri"), imageUrl);
//        executePrint(accessMap.get("access_token"), accessMap.get("subject_id"), jobMap.get("id"));
//    }
//
//    private Map<String, String> getAccessToken(String printerEmail) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        var map = new LinkedMultiValueMap<String, String>();
//        map.add("grant_type", "password");
//        map.add("username", printerEmail);
//        map.add("password", "");  // Assuming no password required
//
//        var request = new HttpEntity<>(map, headers);
//        var response = restTemplate.postForEntity(EPSON_CONNECT_API_ENDPOINT + "/api/1/printing/oauth2/auth/token?subject=printer", request, Map.class);
//        return response.getBody();
//    }
//
//    private Map<String, String> createJob(String token, String deviceId, String jobName) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(token);
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        var map = new LinkedMultiValueMap<String, String>();
//        map.add("job_name", jobName);
//        map.add("print_mode", "photo");  // Assuming printing photos
//
//        var request = new HttpEntity<>(map, headers);
//        var response = restTemplate.postForEntity(EPSON_CONNECT_API_ENDPOINT + "/api/1/printing/printers/{device_id}/jobs", request, Map.class, deviceId);
//        return response.getBody();
//    }
//
//    private void uploadImage(String token, String uploadUri, String imageUrl) {
//        byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(token);
//        headers.setContentLength(imageBytes.length);
//        headers.setContentType(MediaType.IMAGE_JPEG);
//
//        var request = new HttpEntity<>(imageBytes, headers);
//        restTemplate.postForEntity("{upload_uri}&File=1.{extension}\n", request, Void.class, uploadUri, "jpg");
//    }
//
//    private void executePrint(String token, String deviceId, String jobId) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(token);
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        HttpEntity<?> request = new HttpEntity<>(headers);
//        restTemplate.postForObject(EPSON_CONNECT_API_ENDPOINT + "/printers/{device_id}/jobs/{job_id}/print", request, Void.class, deviceId, jobId);
//    }
//}

