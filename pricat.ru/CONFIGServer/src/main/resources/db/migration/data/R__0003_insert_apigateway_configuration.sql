-- Insert API Gateway configuration properties into config_properties table
-- Application name: api-gateway
-- Profile: default (unless specified otherwise)
-- Label: master (default)

-- app.security.access-token-max-age
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'app.security.access-token-max-age', '3600', 'Access token maximum age in seconds');

-- app.security.refresh-token-max-age
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'app.security.refresh-token-max-age', '43000', 'Refresh token maximum age in seconds');

-- server.compression.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'server.compression.enabled', 'true', 'Enable server response compression');

-- server.compression.min-response-size
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'server.compression.min-response-size', '2KB', 'Minimum response size for compression');

-- spring.main.web-application-type
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.main.web-application-type', 'reactive', 'Web application type for Spring Boot');

-- spring.cloud.gateway.server.webflux.default-filters
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.default-filters', 'DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin', 'Default filters for Spring Cloud Gateway');

-- spring.cloud.gateway.server.webflux.discovery.locator.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.discovery.locator.enabled', 'true', 'Enable discovery locator for Spring Cloud Gateway');

-- spring.cloud.gateway.server.webflux.discovery.locator.lower-case-service-id
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.discovery.locator.lower-case-service-id', 'true', 'Use lowercase service IDs in discovery locator');

-- spring.cloud.config.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.config.enabled', 'true', 'Enable Spring Cloud Config client');

-- spring.cloud.config.uri
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.config.uri', 'http://localhost:8888', 'Spring Cloud Config server URI');

-- spring.cloud.config.fail-fast
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.config.fail-fast', 'true', 'Fail fast if config server is not available');

-- spring.cloud.config.retry.initial-interval
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.config.retry.initial-interval', '5000', 'Initial retry interval for config server connection');

-- spring.cloud.config.retry.max-attempts
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.config.retry.max-attempts', '3', 'Maximum retry attempts for config server connection');

-- spring.cloud.config.retry.max-interval
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.config.retry.max-interval', '5000', 'Maximum retry interval for config server connection');

-- spring.cloud.config.retry.multiplier
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.config.retry.multiplier', '1.2', 'Retry interval multiplier');

-- spring.config.import
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.config.import', 'optional:configserver:http://localhost:8888', 'Import configuration from Spring Cloud Config server');

-- spring.http.codecs.max-in-memory-size
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.http.codecs.max-in-memory-size', '10MB', 'Max in-memory size for HTTP codecs');

-- eureka.client.service-url.defaultZone
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/', 'Eureka server URL');

-- eureka.client.register-with-eureka
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'eureka.client.register-with-eureka', 'true', 'Register with Eureka server');

-- eureka.client.fetch-registry
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'eureka.client.fetch-registry', 'true', 'Fetch Eureka registry');

-- eureka.instance.prefer-ip-address
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'eureka.instance.prefer-ip-address', 'false', 'Prefer IP address over hostname for Eureka instance');

-- eureka.instance.hostname
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'eureka.instance.hostname', 'localhost', 'Hostname for Eureka instance');

-- management.endpoints.web.exposure.include
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'management.endpoints.web.exposure.include', 'health, info, gateway, metrics, logfile', 'Exposed management endpoints');

-- management.endpoints.web.base-path
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'management.endpoints.web.base-path', '/actuator', 'Base path for management endpoints');

-- management.endpoint.gateway.access
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'management.endpoint.gateway.access', 'read_only', 'Access level for gateway endpoint');

-- management.endpoint.health.show-details
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'management.endpoint.health.show-details', 'always', 'Show health details');

-- management.endpoint.health.show-components
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'management.endpoint.health.show-components', 'always', 'Show health components');

-- management.endpoint.logfile.access
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'management.endpoint.logfile.access', 'read_only', 'Access level for logfile endpoint');

-- management.endpoint.info.access
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'management.endpoint.info.access', 'read_only', 'Access level for info endpoint');

-- management.health.defaults.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'management.health.defaults.enabled', 'true', 'Enable default health indicators');

-- logging.file.name
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'logging.file.name', './logs/api-gateway.log', 'Log file name');

-- logging.config
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'logging.config', 'classpath:logback.xml', 'Logback configuration file location');

-- logging.level.ru.pricat
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'logging.level.ru.pricat', 'DEBUG', 'Log level for ru.pricat package');

-- logging.level.io.r2dbc
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'logging.level.io.r2dbc', 'WARN', 'Log level for io.r2dbc package');

-- logging.level.org.flywaydb
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'logging.level.org.flywaydb', 'WARN', 'Log level for org.flywaydb package');

-- logging.level.org.springframework.web
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'logging.level.org.springframework.web', 'WARN', 'Log level for org.springframework.web package');

-- logging.level.org.springframework.jdbc
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'logging.level.org.springframework.jdbc', 'WARN', 'Log level for org.springframework.jdbc package');

-- logging.level.org.springframework.security
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'logging.level.org.springframework.security', 'INFO', 'Log level for org.springframework.security package');

-- logging.level.org.springframework.cloud.config
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'logging.level.org.springframework.cloud.config', 'WARN', 'Log level for org.springframework.cloud.config package');

-- logging.level.org.springframework.r2dbc
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'logging.level.org.springframework.r2dbc', 'WARN', 'Log level for org.springframework.r2dbc package');

-- logging.level.com.netflix.discovery.shared.resolver.aws.ConfigClusterResolver
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'logging.level.com.netflix.discovery.shared.resolver.aws.ConfigClusterResolver', 'WARN', 'Log level for ConfigClusterResolver');

-- Gateway routes configuration (these would be complex to map directly to simple key-value, but we can try)
-- spring.cloud.gateway.server.webflux.routes[0].id
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[0].id', 'client-service-web', 'Route ID for client-service-web');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[0].uri', 'lb://client-service', 'URI for client-service-web route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[0].predicates[0]', 'Path=/login, /register, /profile, /profile-edit, /clients, /files, /admin, /user/**', 'Predicates for client-service-web route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[0].filters[0]', 'AddForwardedHeadersFilter', 'Filters for client-service-web route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[1].id', 'client-service-static', 'Route ID for client-service-static');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[1].uri', 'lb://client-service', 'URI for client-service-static route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[1].filters[0]', 'AddForwardedHeadersFilter', 'Filters for client-service-static route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[1].predicates[0]', 'Path=/webjars/**, /js/**, /css/**, /images/**', 'Predicates for client-service-static route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[2].id', 'client-service-api', 'Route ID for client-service-api');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[2].uri', 'lb://client-service', 'URI for client-service-api route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[2].predicates[0]', 'Path=/api/v1/client/**, api/v1/client/debug-headers/**', 'Predicates for client-service-api route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[2].filters[0]', 'AddForwardedHeadersFilter', 'Filters for client-service-api route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[3].id', 'auth-service-login', 'Route ID for auth-service-login');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[3].uri', 'lb://auth-service', 'URI for auth-service-login route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[3].predicates[0]', 'Path=/api/v1/auth/login', 'Predicates for auth-service-login route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[3].filters[0]', 'AddXRequestIdFilter', 'Filters for auth-service-login route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[3].filters[1]', 'SetRefreshTokenCookieFilter', 'Filters for auth-service-login route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[3].filters[2]', 'HandleRefreshTokenFilter', 'Filters for auth-service-login route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[3].filters[3]', 'ClearRefreshTokenCookieFilter', 'Filters for auth-service-login route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[3].filters[4]', 'SetAccessTokenCookie', 'Filters for auth-service-login route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[3].filters[5]', 'RemoveRequestHeader=X-Refresh-Token', 'Filters for auth-service-login route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[4].id', 'auth-service-other', 'Route ID for auth-service-other');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[4].uri', 'lb://auth-service', 'URI for auth-service-other route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[4].predicates[0]', 'Path=/api/v1/auth/**', 'Predicates for auth-service-other route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[4].filters[0]', 'AddXRequestIdFilter', 'Filters for auth-service-other route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[4].filters[1]', 'SetRefreshTokenCookieFilter', 'Filters for auth-service-other route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[4].filters[2]', 'HandleRefreshTokenFilter', 'Filters for auth-service-other route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[4].filters[3]', 'ClearRefreshTokenCookieFilter', 'Filters for auth-service-other route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[4].filters[4]', 'RemoveRequestHeader=X-Refresh-Token', 'Filters for auth-service-other route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[5].id', 'search-service-web', 'Route ID for search-service-web');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[5].uri', 'lb://search-service', 'URI for search-service-web route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[5].predicates[0]', 'Path=/search', 'Predicates for search-service-web route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[5].filters[0]', 'AddForwardedHeadersFilter', 'Filters for search-service-web route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[6].id', 'search-service-api', 'Route ID for search-service-api');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[6].uri', 'lb://search-service', 'URI for search-service-api route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[6].predicates[0]', 'Path=/api/v1/search/**', 'Predicates for search-service-api route');

INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('api-gateway', 'default', 'master', 'spring.cloud.gateway.server.webflux.routes[6].filters[0]', 'AddForwardedHeadersFilter', 'Filters for search-service-api route');