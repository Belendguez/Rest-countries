package com.restcountries.controller;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.restcountries.dto.CountryDetailsDto;
import com.restcountries.dto.NeighborCountryDto;
import org.springframework.core.ParameterizedTypeReference;

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
    @GetMapping("/{nameOrCode}")
    public ResponseEntity<Object> getCountryInfo(@PathVariable String nameOrCode) {
        try {
            // Log de la URL que se va a consultar
            String url = apiUrl + "/v3.1/name/" + nameOrCode + "?fullText=true";
            System.out.println("Fetching data from URL: " + url);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            // Verifica la respuesta
            System.out.println("API response: " + response.getBody());

            if (response.getBody() == null || response.getBody().isEmpty()) {
                System.out.println("Country not found.");
                return ResponseEntity.status(404).body("El país '" + nameOrCode + "' no fue encontrado.");
            }

            Map<String, Object> countryData = response.getBody().get(0);

            // Procesar y verificar cada campo
            CountryDetailsDto countryDetails = new CountryDetailsDto();
            countryDetails.setOfficialName(getNestedValue(countryData, "name", "official"));
            countryDetails.setCapital(getFirstElement((List<String>) countryData.get("capital")));
            countryDetails.setRegion((String) countryData.get("region"));

            // Procesar la población
            Object populationObj = countryData.get("population");
            Long population = null;
            if (populationObj instanceof Long) {
                population = (Long) populationObj;
            } else if (populationObj instanceof Integer) {
                population = ((Integer) populationObj).longValue();
            }
            countryDetails.setPopulation(population);

            // Procesar la URL de la bandera
            countryDetails.setFlagUrl(getNestedValue(countryData, "flags", "png"));
            countryDetails.setOfficialLanguages((Map<String, String>) countryData.get("languages"));

            // Procesar las fronteras y obtener los países vecinos
            List<String> borders = (List<String>) countryData.get("borders");
            List<NeighborCountryDto> neighbors = getNeighborDetails(borders);
            countryDetails.setNeighbors(neighbors);

            System.out.println("Country details: " + countryDetails);

            return ResponseEntity.ok(countryDetails);  // Devuelve los detalles del país
        } catch (HttpClientErrorException.NotFound e) {
            // En caso de error 404 de la API externa
            return ResponseEntity.status(404).body("El país '" + nameOrCode + "' no fue encontrado.");
        } catch (Exception e) {
            // Manejo de otros errores
            return ResponseEntity.status(500).body("Ocurrió un error interno en el servidor.");
        }
    }

    private List<NeighborCountryDto> getNeighborDetails(List<String> borders) {
        List<NeighborCountryDto> neighbors = new ArrayList<>();
        if (borders == null || borders.isEmpty()) {
            return neighbors;
        }

        for (String border : borders) {
            String neighborUrl = apiUrl + "/v3.1/alpha/" + border;
            try {
                ResponseEntity<List<Map<String, Object>>> neighborResponse = restTemplate.exchange(
                        neighborUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        }
                );

                if (neighborResponse.getBody() != null && !neighborResponse.getBody().isEmpty()) {
                    Map<String, Object> neighborData = neighborResponse.getBody().get(0);
                    NeighborCountryDto neighbor = new NeighborCountryDto();

                    neighbor.setName(getNestedValue(neighborData, "name", "common"));
                    neighbor.setCapital(getFirstElement((List<String>) neighborData.get("capital")));
                    Object populationObj = neighborData.get("population");
                    Long populationC = null;
                    if (populationObj instanceof Long) {
                        populationC = (Long) populationObj;
                    } else if (populationObj instanceof Integer) {
                        populationC = ((Integer) populationObj).longValue();
                    }
                    neighbor.setPopulation(populationC);
                    neighbors.add(neighbor);
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch data for neighbor: " + border + " - " + e.getMessage());
            }
        }

        return neighbors;
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

    @Cacheable(value = "neighbors", key = "#countryName")
    @GetMapping("/{countryName}/neighbors")
    public ResponseEntity<Object> getNeighbors(@PathVariable String countryName) {
        try {
            // Realizamos la consulta usando el nombre del país para obtener los detalles del país
            String url = "https://restcountries.com/v3.1/name/" + countryName;
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, null, List.class);

            // Verificamos si la respuesta es exitosa (200 OK) y si la lista no está vacía
            if (response.getStatusCode().is2xxSuccessful() && !response.getBody().isEmpty()) {
                // Extraemos el primer país de la lista (en caso de que haya más de uno)
                Map<String, Object> country = (Map<String, Object>) response.getBody().get(0);

                // Obtenemos los países vecinos (si existen) desde el campo "borders"
                List<String> neighbors = (List<String>) country.get("borders");

                if (neighbors != null && !neighbors.isEmpty()) {
                    // Traducimos los códigos alfa-3 a nombres de países
                    List<String> neighborNames = new ArrayList<>();
                    for (String neighborCode : neighbors) {
                        String neighborUrl = "https://restcountries.com/v3.1/alpha/" + neighborCode;
                        ResponseEntity<List> neighborResponse = restTemplate.exchange(neighborUrl, HttpMethod.GET, null, List.class);

                        if (neighborResponse.getStatusCode().is2xxSuccessful() && !neighborResponse.getBody().isEmpty()) {
                            Map<String, Object> neighborCountry = (Map<String, Object>) neighborResponse.getBody().get(0);

                            // Obtener el nombre común del país vecino
                            Map<String, String> name = (Map<String, String>) neighborCountry.get("name");
                            String neighborName = name.get("common");  // Utilizamos 'common' para el nombre corto

                            neighborNames.add(neighborName);
                        } else {
                            // Si no encontramos información del vecino, lo manejamos
                            neighborNames.add("Desconocido");
                        }
                    }
                    return ResponseEntity.ok(neighborNames); // Devolvemos la lista de nombres de países vecinos
                } else {
                    return ResponseEntity.ok(Collections.emptyList()); // Si no tiene vecinos, devolvemos una lista vacía
                }
            } else {
                // Si la respuesta de la API externa no es exitosa
                String errorMessage = "El país '" + countryName + "' no fue encontrado o no es válido. Respuesta: " + response.getStatusCode();
                return ResponseEntity.status(404).body(errorMessage);
            }
        } catch (HttpClientErrorException.NotFound e) {
            // Manejo explícito de error 404 cuando la API no encuentra el país
            return ResponseEntity.status(404).body("El país '" + countryName + "' no fue encontrado.");
        } catch (Exception e) {
            // Capturamos cualquier otra excepción y mostramos un mensaje más detallado
            return ResponseEntity.status(500).body("Error al obtener los datos: " + e.getMessage());
        }
    }

}