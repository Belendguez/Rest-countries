package com.restcountries.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String home() {
        return "Bienvenido a la API de paises.\n"+"Usa /country/{nameOrCode} para obtener informacion de un pais.\n"+
                "Usa /country/{nameOrCode}/neighbors para obtener informacion de los paises limitrofes a dicho pais.\n";

    }
}
