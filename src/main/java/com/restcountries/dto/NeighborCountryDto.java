package com.restcountries.dto;

public class NeighborCountryDto {
    private String name;
    private String capital;
    private Number population;

    // Getters y Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public Number getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }
}
