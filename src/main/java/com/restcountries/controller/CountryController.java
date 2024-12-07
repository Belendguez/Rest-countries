package com.restcountries.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/country")
public class CountryController {

    // URL de la API pública de REST Countries
    @Value("${restcountries.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public CountryController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Endpoint para obtener información del país por nombre o código ISO
    @GetMapping("/{nameOrCode}")
    public ResponseEntity<String> getCountryInfo(@PathVariable String nameOrCode) {
        // Construir la URL para consultar el país
        String url = apiUrl + "/v3.1/name/" + nameOrCode; // Para búsqueda por nombre
        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);  // Retornar información del país
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener datos del país");
        }
    }

    // Endpoint para obtener los países vecinos
    @GetMapping("/{nameOrCode}/neighbors")
    public ResponseEntity<String> getCountryNeighbors(@PathVariable String nameOrCode) {
        // Usamos el código alfa del país para obtener los países vecinos
        String url = apiUrl + "/v3.1/alpha/" + nameOrCode + "/borders"; // Ajuste en la URL
        try {
            String response = restTemplate.getForObject(url, String.class);

            // Si la respuesta es vacía o nula, no hay países vecinos o no se encontraron
            if (response == null || response.isEmpty()) {
                return ResponseEntity.notFound().build();  // Retorna un 404 si no hay vecinos
            }

            return ResponseEntity.ok(response);  // Retorna la información de los países vecinos
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener países vecinos");
        }
    }
}
