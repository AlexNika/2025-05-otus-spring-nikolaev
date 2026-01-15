### Sequence Diagram: Пользователь заходит на http://localhost:8080/search, access-token валиден, токены не обновляются, доступ разрешён.

```mermaid
sequenceDiagram
    participant B as Browser
    participant G as api-gateway
    participant S as search-service
    participant TRF as TokenRefreshFilter
    participant JAF as JwtAuthenticationFilter
    participant JD as JwtDecoder
    participant JGAC as JwtGrantedAuthoritiesConverter
    participant SC as SecurityConfig
    participant SCH as SearchController
    participant SS as SearchService
    participant ES as Elasticsearch
    participant TH as Thymeleaf
    participant SRS as SearchRestController

    B->>G: GET /search (with cookies: access-token, refresh-token)
    G->>S: GET /search (with cookies)
    S->>TRF: request received
    TRF->>JD: decode(access-token)
    JD->>JD: validate token, check exp
    TRF->>TRF: isTokenExpiringSoon? -> NO
    TRF->>JAF: continue filter chain
    JAF->>JD: decode(access-token)
    JD->>JGAC: extract roles
    JAF->>S: setAuthentication(user, roles)
    JAF->>SC: check authorization
    SC->>SC: authenticated()? -> YES
    JAF->>SCH: continue
    SCH->>SS: search(query, company, pageable)
    SS->>ES: execute search query
    ES->>SS: return search results
    SS->>SCH: return Page<PriceItemDocument>
    SCH->>TH: render search.html with model
    TH->>B: return HTML page

    B->>S: fetch /api/v1/search (with Authorization: Bearer <access-token>)
    S->>JAF: request received
    JAF->>JD: decode(access-token from header)
    JD->>JGAC: extract roles
    JAF->>S: setAuthentication(user, roles)
    JAF->>SC: check authorization
    SC->>SC: authenticated()? -> YES
    JAF->>SRS: continue
    SRS->>SS: search(query, company, pageable)
    SS->>ES: execute search query
    ES->>SS: return search results
    SS->>SRS: return Page<PriceItemDocument>
    SRS->>B: return JSON results