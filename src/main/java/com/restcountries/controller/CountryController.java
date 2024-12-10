package com.restcountries.controller;

import com.restcountries.dto.CountryDetailsDto;
import com.restcountries.dto.NeighborCountryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/country")
public class CountryController {

    @Value("${restcountries.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public CountryController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = "countries", key = "#nameOrCode")
    @Operation(summary = "Get country details", description = "Fetches details of a country by its name or alpha code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Country details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Country not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{nameOrCode}")
    public ResponseEntity<Object> getCountryInfo(
            @Parameter(description = "Name or alpha code of the country", example = "Spain or ES")
            @PathVariable String nameOrCode) {
        try {
            String url;
            ResponseEntity<List<Map<String, Object>>> response;

            // Attempt to fetch by full name
            url = apiUrl + "/v3.1/name/" + nameOrCode + "?fullText=true";
            response = fetchApiResponse(url);

            // If not found, try alpha code
            if (response.getBody() == null || response.getBody().isEmpty()) {
                url = apiUrl + "/v3.1/alpha/" + nameOrCode;
                response = fetchApiResponse(url);
            }

            // If still not found
            if (response.getBody() == null || response.getBody().isEmpty()) {
                return ResponseEntity.status(404).body("El país '" + nameOrCode + "' no fue encontrado.");
            }

            Map<String, Object> countryData = response.getBody().get(0);
            CountryDetailsDto countryDetails = buildCountryDetails(countryData);
            return ResponseEntity.ok(countryDetails);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(404).body("El país '" + nameOrCode + "' no fue encontrado.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ocurrió un error interno en el servidor.");
        }
    }

    @Cacheable(value = "neighbors", key = "#countryName")
    @Operation(summary = "Get neighboring countries", description = "Fetches a list of neighboring countries for a given country.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Neighboring countries retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Country not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{countryName}/neighbors")
    public ResponseEntity<Object> getNeighbors(
            @Parameter(description = "Name or alpha code of the country", example = "France or FRA")
            @PathVariable String countryName) {
        try {
            String url = apiUrl + "/v3.1/name/" + countryName + "?fullText=true";
            ResponseEntity<List<Map<String, Object>>> response = fetchApiResponse(url);

            // If not found, try alpha code
            if (response.getBody() == null || response.getBody().isEmpty()) {
                url = apiUrl + "/v3.1/alpha/" + countryName;
                response = fetchApiResponse(url);
            }

            // If still not found
            if (response.getBody() == null || response.getBody().isEmpty()) {
                return ResponseEntity.status(404).body("El país '" + countryName + "' no fue encontrado.");
            }

            Map<String, Object> countryData = response.getBody().get(0);
            List<String> borders = (List<String>) countryData.get("borders");

            if (borders == null || borders.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList()); // No neighbors
            }

            List<String> neighborNames = fetchNeighborNames(borders);
            return ResponseEntity.ok(neighborNames);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(404).body("El país '" + countryName + "' no fue encontrado.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener los datos: " + e.getMessage());
        }
    }


    // Helper Methods
    private ResponseEntity<List<Map<String, Object>>> fetchApiResponse(String url) {
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(404).body(Collections.emptyList());
        }
    }

    private CountryDetailsDto buildCountryDetails(Map<String, Object> countryData) {
        CountryDetailsDto details = new CountryDetailsDto();

        details.setOfficialName(getNestedValue(countryData, "name", "official"));
        details.setCapital(getFirstElement((List<String>) countryData.get("capital")));
        details.setRegion((String) countryData.get("region"));
        details.setPopulation(getPopulationValue(countryData.get("population")));
        details.setFlagUrl(getNestedValue(countryData, "flags", "png"));
        details.setOfficialLanguages((Map<String, String>) countryData.get("languages"));

        List<String> borders = (List<String>) countryData.get("borders");
        details.setNeighbors(getNeighborDetails(borders));

        return details;
    }

    private List<NeighborCountryDto> getNeighborDetails(List<String> borders) {
        if (borders == null || borders.isEmpty()) {
            return Collections.emptyList();
        }

        List<NeighborCountryDto> neighbors = new ArrayList<>();
        for (String border : borders) {
            try {
                String neighborUrl = apiUrl + "/v3.1/alpha/" + border;
                ResponseEntity<List<Map<String, Object>>> response = fetchApiResponse(neighborUrl);

                if (response.getBody() != null && !response.getBody().isEmpty()) {
                    Map<String, Object> neighborData = response.getBody().get(0);
                    NeighborCountryDto neighbor = new NeighborCountryDto();
                    neighbor.setName(getNestedValue(neighborData, "name", "common"));
                    neighbor.setCapital(getFirstElement((List<String>) neighborData.get("capital")));
                    neighbor.setPopulation(getPopulationValue(neighborData.get("population")));
                    neighbors.add(neighbor);
                }
            } catch (Exception e) {
                System.err.println("Error fetching data for neighbor: " + border + " - " + e.getMessage());
            }
        }
        return neighbors;
    }

    private List<String> fetchNeighborNames(List<String> borders) {
        List<String> neighborNames = new ArrayList<>();
        for (String border : borders) {
            try {
                String neighborUrl = apiUrl + "/v3.1/alpha/" + border;
                ResponseEntity<List<Map<String, Object>>> response = fetchApiResponse(neighborUrl);

                if (response.getBody() != null && !response.getBody().isEmpty()) {
                    Map<String, Object> neighborData = response.getBody().get(0);
                    neighborNames.add(getNestedValue(neighborData, "name", "common"));
                }
            } catch (Exception e) {
                neighborNames.add("Desconocido");
            }
        }
        return neighborNames;
    }

    private Long getPopulationValue(Object populationObj) {
        if (populationObj instanceof Long) {
            return (Long) populationObj;
        } else if (populationObj instanceof Integer) {
            return ((Integer) populationObj).longValue();
        }
        return null;
    }

    private String getNestedValue(Map<String, Object> map, String key, String nestedKey) {
        if (map.containsKey(key) && map.get(key) instanceof Map) {
            Map<String, Object> nestedMap = (Map<String, Object>) map.get(key);
            return (String) nestedMap.get(nestedKey);
        }
        return null;
    }

    private String getFirstElement(List<String> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

}