package com.restcountries.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String home() {
        return "Bienvenido a la API de países.\n"+"Usa /country/{nameOrCode} para obtener información de un país.\n"+
                "Usa /country/{nameOrCode}/neighbors para obtener información de los paises limitrofes a dicho pais.\n";

    }
}
