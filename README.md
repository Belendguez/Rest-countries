Proyecto REST-COUNTRIES
El proyecto esta codificado en Java 11.

Instalar dependecias
mvn clean install

Ejecuta la aplicación Spring Boot 
mvn spring-boot:run

Abrir la página home
http://localhost:8080

Abrir Swagger
http://localhost:8080/swagger-ui/index.html

Para hacer pruebas, lo mas sencillo es abrir el navegador ha introducir las siguientes direcciones
- Nombre: http://localhost:8080/country/Spain        
- Alpha code 2: http://localhost:8080/country/ES
- Alpha code 3: http://localhost:8080/country/ESP
- http://localhost:8080/country/Spain/neighbors
- http://localhost:8080/country/ES/neighbors
- http://localhost:8080/country/ESP/neighbors

Error Pais no existe
- http://localhost:8080/country/AAA/neighbors
- http://localhost:8080/country/AAA

Pais sin vecinos
- http://localhost:8080/country/Australia
- http://localhost:8080/country/Australia/neighbors


Resultados de la busqueda Spain: 

{
  "officialName": "Kingdom of Spain",
  "capital": "Madrid",
  "region": "Europe",
  "population": 47351567,
  "officialLanguages": {
    "spa": "Spanish",
    "cat": "Catalan",
    "eus": "Basque",
    "glc": "Galician"
  },
  "flagUrl": "https://flagcdn.com/w320/es.png",
  "neighbors": [
    {
      "name": "Andorra",
      "capital": "Andorra la Vella",
      "population": 77265
    },
    {
      "name": "France",
      "capital": "Paris",
      "population": 67391582
    },
    {
      "name": "Gibraltar",
      "capital": "Gibraltar",
      "population": 33691
    },
    {
      "name": "Portugal",
      "capital": "Lisbon",
      "population": 10305564
    },
    {
      "name": "Morocco",
      "capital": "Rabat",
      "population": 36910558
    }
  ]
}

Resultados de la busqueda de los vecinos de Spain:

[
  "Andorra",
  "France",
  "Gibraltar",
  "Portugal",
  "Morocco"
]

Resultado de la busqueda de un pais que no existe:

El pai­s 'AAA' no fue encontrado.

Resultado de la busqueda de un pais sin vecinos:

{
  "officialName": "Commonwealth of Australia",
  "capital": "Canberra",
  "region": "Oceania",
  "population": 25687041,
  "officialLanguages": {
    "eng": "English"
  },
  "flagUrl": "https://flagcdn.com/w320/au.png",
  "neighbors": []
}

Resultados de la busqueda de los vecinos de un pais sin vecinos:

[]
