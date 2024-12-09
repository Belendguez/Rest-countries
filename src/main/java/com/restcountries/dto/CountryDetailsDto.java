package com.restcountries.dto;

import java.util.List;
import java.util.Map;

public class CountryDetailsDto {
    private String officialName;
    private String capital;
    private String region;
    private Number population;
    private Map<String, String> officialLanguages; // Idiomas oficiales
    private String flagUrl;
    private List<NeighborCountryDto> neighbors;

    // Getters y Setters
    public String getOfficialName() {
        return officialName;
    }

    public void setOfficialName(String officialName) {
        this.officialName = officialName;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Number getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public Map<String, String> getOfficialLanguages() {
        return officialLanguages;
    }

    public void setOfficialLanguages(Map<String, String> officialLanguages) {
        this.officialLanguages = officialLanguages;
    }

    public String getFlagUrl() {
        return flagUrl;
    }

    public void setFlagUrl(String flagUrl) {
        this.flagUrl = flagUrl;
    }

    public List<NeighborCountryDto> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<NeighborCountryDto> neighbors) {
        this.neighbors = neighbors;
    }

}
