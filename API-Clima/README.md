# 🌦️ API-Clima

Projeto desenvolvido com **Spring Boot** que consome a **API do Open-Meteo** para obter informações meteorológicas.

A aplicação disponibiliza endpoints REST que consultam os **dados meteorológicos atuais** de **Belo Horizonte - MG**, com a possibilidade de informar outras cidades.

---

# 👥 Integrantes

* Arthur Gabriel 
* Pedro Felipe

---

# 🛠 Tecnologias

* Java 25
* Spring Boot 4.1.1
* Maven
* REST API
* RestClient
* Jackson

---

# 🌐 API Utilizada

Este projeto consome a API:

- https://open-meteo.com/

Endpoints utilizados:

- https://api.open-meteo.com/v1/forecast
- https://geocoding-api.open-meteo.com/v1/search

A API do Open-Meteo é gratuita e **não exige token** para uso não comercial.

---

# ⚡ Funcionalidades

A API permite realizar:

* ☁️ Consulta de **dados meteorológicos atuais**
* 📍 Consulta do clima de **Belo Horizonte - MG**
* 🔎 Consulta do clima de **outras cidades** informadas pelo usuário
* 🧭 Tradução do **código do tempo** e da **direção do vento** para português
* 🔗 Consumo de **API externa utilizando RestClient**
* ⚠️ Tratamento de **erros de comunicação** e **dados indisponíveis**

---

# 🚀 Endpoints

## 📌 Consultar clima de Belo Horizonte

GET /clima

Exemplo:

- http://localhost:8080/clima

---

## 📌 Consultar clima informando a cidade

GET /clima?cidade={nome}

Exemplo:

- http://localhost:8080/clima?cidade=Ouro Preto

---

## 📌 Consultar clima com a cidade na URL

GET /clima/cidade/{nome}

Exemplo:

- http://localhost:8080/clima/cidade/Lisboa

---

## 📌 Consultar clima de Belo Horizonte pela rota

GET /clima/belo-horizonte

Exemplo:

- http://localhost:8080/clima/belo-horizonte

---

# 📄 Exemplo de Resposta

```
{
  "cidade": "Belo Horizonte",
  "estado": "Minas Gerais",
  "pais": "Brasil",
  "latitude": -19.9167,
  "longitude": -43.9345,
  "dataHoraConsulta": "2026-08-25T15:42:10.318",
  "temperatura": 26.4,
  "temperaturaMaxima": 29.8,
  "temperaturaMinima": 16.2,
  "umidade": 42,
  "velocidadeVento": 11.5,
  "direcaoVentoGraus": 128,
  "direcaoVento": "Sudeste",
  "descricao": "Parcialmente nublado"
}
```

Unidades: temperatura em °C, umidade em %, vento em km/h.

---

# ⚠️ Tratamento de Erros

| Situação | Status |
|----------|--------|
| Cidade informada não encontrada | 404 |
| Falha na comunicação com a API externa | 503 |
| Dados meteorológicos indisponíveis | 503 |

---

# 📁 Estrutura do Projeto

```
API-Clima
│
└── src/main/java/com/example/API_Clima
    │
    ├── ApiClimaApplication.java
    │
    ├── controller
    │   └── ClimaController.java
    │
    ├── service
    │   └── ClimaService.java
    │
    └── dto
        ├── ClimaResponse.java
        ├── OpenMeteoResponse.java
        └── GeocodingResponse.java
│
└── src/main/resources
    │
    └── application.properties
│
└── pom.xml
```

---

# ⚙️ Configuração

No arquivo **application.properties** ficam as URLs da API externa:

```
clima.api.url=https://api.open-meteo.com/v1/forecast
clima.api.geocoding-url=https://geocoding-api.open-meteo.com/v1/search
clima.api.key=${CLIMA_API_KEY:}
```

O Open-Meteo **não exige API Key**, por isso a propriedade fica vazia.

Caso a API externa seja trocada por outra que exija token (WeatherAPI, OpenWeather, Tomorrow.io), a chave **não deve ser escrita no arquivo**. Defina a variável de ambiente:

```
export CLIMA_API_KEY=seu_token_aqui
```

No Windows:

```
set CLIMA_API_KEY=seu_token_aqui
```

E injete no código com `@Value("${clima.api.key}")`.

---

# ▶️ Como Executar

### 1️⃣ Clonar o repositório

```
git clone https://github.com/arturity7/API-Clima.git
```

---

### 2️⃣ Entrar na pasta do projeto

```
cd API-Clima
```

---

### 3️⃣ Executar a aplicação

```
./mvnw spring-boot:run
```

No Windows:

```
mvnw.cmd spring-boot:run
```

---

### 4️⃣ Acessar a API

Exemplo no navegador ou Postman:

- http://localhost:8080/clima

---

# 📦 Dependências

```
spring-boot-starter-webmvc
spring-boot-starter-webmvc-test
```

O RestClient e o Jackson já vêm inclusos no starter, portanto nenhuma dependência adicional foi necessária.

---

# 📚 Links úteis

Spring Boot  
- https://spring.io/projects/spring-boot

Maven  
- https://maven.apache.org/

Open-Meteo  
- https://open-meteo.com/

---

# 🛡 Licença

Este projeto está sob a licença **MIT**.

Dados meteorológicos fornecidos por Open-Meteo.com sob licença CC BY 4.0.