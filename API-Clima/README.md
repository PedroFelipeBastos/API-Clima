# 🌦️ API-Clima — Clima de Belo Horizonte com Spring Boot

API REST em **Java + Spring Boot** que consome uma API externa de dados
meteorológicos ([Open-Meteo](https://open-meteo.com)), processa a resposta e
disponibiliza o clima de **Belo Horizonte – MG** em um endpoint próprio.

> Atividade 01 — Desenvolvimento e Integração de Aplicações Web

## 👥 Integrantes

| Nome | Matrícula |
|------|-----------|
| _preencher_ | _preencher_ |
| _preencher_ | _preencher_ |

---

## 🧰 Tecnologias e dependências

| Item | Versão / Artefato |
|------|-------------------|
| Java | 25 |
| Spring Boot | 4.1.1 |
| Web / REST | `spring-boot-starter-webmvc` |
| Testes | `spring-boot-starter-webmvc-test` |
| Build | Maven (via Maven Wrapper — `mvnw`) |
| Cliente HTTP | `RestClient` (Spring Framework) |
| JSON | Jackson 3 (já incluso no starter) |
| API externa | Open-Meteo Forecast API |

Nenhuma dependência extra foi adicionada ao `pom.xml`: o `RestClient` e o
Jackson já vêm com o starter web.

**Por que Open-Meteo?** É gratuita, **não exige API Key** e entrega todos os
dados pedidos no enunciado.

---

## 📁 Estrutura do projeto

```
src/main/java/com/example/API_Clima/
├── ApiClimaApplication.java      # classe principal
├── controller/
│   └── ClimaController.java      # endpoints REST
├── service/
│   └── ClimaService.java         # chama a API externa e monta a resposta
└── dto/
    ├── ClimaResponse.java        # objeto de saída da NOSSA API
    ├── OpenMeteoResponse.java    # espelha o JSON do clima
    └── GeocodingResponse.java    # espelha o JSON da geocodificação
```

Os dois DTOs ficam separados de propósito: se o Open-Meteo mudar o formato
dele, o contrato do nosso endpoint não quebra.

---

## ▶️ Como executar localmente

**Pré-requisitos:** JDK 25 instalado e conexão com a internet.
O Maven **não** precisa estar instalado — o projeto usa o Maven Wrapper.

```bash
git clone https://github.com/SEU-USUARIO/API-Clima.git
cd API-Clima

./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows
```

A aplicação sobe em `http://localhost:8080`.

> Se o seu JDK for outro (21, 24…), altere `<java.version>25</java.version>`
> no `pom.xml` para a sua versão. O Spring Boot 4 exige no mínimo o Java 21.

Testando:

```bash
curl http://localhost:8080/clima
```

Ou abra no navegador: <http://localhost:8080/clima>

Gerando o `.jar`:

```bash
./mvnw clean package
java -jar target/API-Clima-0.0.1-SNAPSHOT.jar
```

---

## 🚀 Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/clima` | Clima de **Belo Horizonte** (padrão, sem parâmetro) |
| `GET` | `/clima?cidade={nome}` | Clima da cidade informada |
| `GET` | `/clima/cidade/{nome}` | Mesma consulta, com a cidade na própria URL |
| `GET` | `/clima/belo-horizonte` | Belo Horizonte, com a cidade explícita na rota |

```bash
curl "http://localhost:8080/clima"
curl "http://localhost:8080/clima?cidade=Ouro%20Preto"
curl "http://localhost:8080/clima/cidade/Lisboa"
```

No navegador dá pra digitar com acento e espaço normalmente:
<http://localhost:8080/clima?cidade=São Paulo>

> Como a cidade é informada por nome, a aplicação primeiro chama a **API de
> geocodificação** do Open-Meteo para descobrir a latitude/longitude, e só
> depois busca o clima. Para Belo Horizonte essa chamada nem acontece: as
> coordenadas estão fixas no `ClimaService`, então o endpoint padrão do
> trabalho continua funcionando mesmo que a geocodificação esteja fora do ar.

**Exemplo de resposta — `200 OK`:**

```json
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

> A API externa não devolve a condição do tempo em texto — devolve um código
> numérico do padrão **WMO 4677**. O `ClimaService` traduz esse código para
> português, e também converte a direção do vento de graus para a rosa dos ventos.

---

## ⚠️ Tratamento de erros

| Situação | Status | Como é tratado |
|----------|--------|----------------|
| Cidade informada não existe | `404` | validação do resultado da geocodificação |
| Falha na comunicação com a API externa | `503` | `try/catch` em `RestClientException` |
| Dados indisponíveis / resposta vazia | `503` | validação da resposta antes de usá-la |

Exemplo de resposta de erro:

```json
{
  "timestamp": "2026-08-25T15:44:02.771+00:00",
  "status": 503,
  "error": "Service Unavailable",
  "path": "/clima"
}
```

---

## 🔐 Configuração da API Key

O **Open-Meteo não exige API Key**, então o projeto roda sem configuração
adicional. Mesmo assim, a estrutura já está pronta caso o provedor seja
trocado (WeatherAPI, OpenWeather, Tomorrow.io).

No `application.properties`:

```properties
clima.api.key=${CLIMA_API_KEY:}
```

A chave **nunca** é escrita no arquivo: ela é lida da variável de ambiente
`CLIMA_API_KEY`, e o `:` no final define string vazia como valor padrão.

```bash
# Linux / macOS
export CLIMA_API_KEY=sua_chave_aqui
./mvnw spring-boot:run

# Windows (cmd)
set CLIMA_API_KEY=sua_chave_aqui
mvnw.cmd spring-boot:run
```

No código, basta injetar com `@Value("${clima.api.key}")`.

> ⚠️ O `.gitignore` já bloqueia `.env` e `application-local.properties`.
> **Nunca faça commit de uma chave real.**

---

## ⚙️ Propriedades configuráveis

| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| `server.port` | `8080` | porta da aplicação |
| `clima.api.url` | `https://api.open-meteo.com/v1/forecast` | endpoint do clima |
| `clima.api.geocoding-url` | `https://geocoding-api.open-meteo.com/v1/search` | endpoint da geocodificação |
| `clima.api.key` | vazio | chave da API externa, via `CLIMA_API_KEY` |

---

## ✅ Requisitos atendidos

- [x] Java + Spring Boot
- [x] API REST própria
- [x] Consumo de API externa de previsão do tempo
- [x] Dados de Belo Horizonte – MG
- [x] Processamento da resposta externa (código WMO e graus traduzidos)
- [x] Endpoint próprio retornando JSON
- [x] API Key configurada de forma adequada (variável de ambiente)
- [x] Estrutura em camadas (controller / service / dto)
- [x] Tratamento básico de erros
- [x] ⭐ Extra: consulta do clima de outras cidades

---

## 📄 Fonte dos dados

Dados meteorológicos fornecidos por [Open-Meteo.com](https://open-meteo.com),
sob licença [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
