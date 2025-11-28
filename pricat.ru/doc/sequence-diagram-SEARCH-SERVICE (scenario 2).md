### Пользователь открывает http://localhost:8080/search, но access-token истёк или истекает в ближайшие 5 минут. 
### TokenRefreshFilter вызывает auth-service для обновления токенов.

```mermaid
sequenceDiagram
    participant B as Browser
    participant G as api-gateway
    participant S as search-service
    participant TRF as TokenRefreshFilter
    participant JD as JwtDecoder
    participant AS as AuthService
    participant AUTH as auth-service
    participant JAF as JwtAuthenticationFilter
    participant JGAC as JwtGrantedAuthoritiesConverter
    participant SC as SecurityConfig
    participant SCH as SearchController
    participant TH as Thymeleaf

    B->>G: GET /search (with cookies: access-token, refresh-token)
    G->>S: GET /search (with cookies)
    S->>TRF: request received
    TRF->>JD: decode(access-token)
    JD->>JD: validate token, check exp
    TRF->>TRF: isTokenExpiringSoon? -> YES
    TRF->>AS: refresh(refresh-token)
    AS->>AUTH: POST /api/v1/auth/refresh (with refresh-token cookie)
    AUTH->>AUTH: validate refresh-token, generate new JWTs
    AUTH->>AS: return new access-token, refresh-token
    AS->>TRF: return LoginResponseDto
    TRF->>S: update access-token, refresh-token cookies
    TRF->>JAF: continue filter chain
    JAF->>JD: decode(new access-token)
    JD->>JGAC: extract roles
    JAF->>S: setAuthentication(user, roles)
    JAF->>SC: check authorization
    SC->>SC: authenticated()? -> YES
    JAF->>SCH: continue
    SCH->>TH: render search.html
    TH->>B: return HTML

    B->>S: fetch /api/v1/search (with Authorization: Bearer <new access-token>)
    S->>JAF: request received
    JAF->>JD: decode(access-token from header)
    JD->>JGAC: extract roles
    JAF->>S: setAuthentication(user, roles)
    JAF->>SC: check authorization
    SC->>SC: authenticated()? -> YES
    JAF->>SCH: continue
    SCH->>B: return JSON results