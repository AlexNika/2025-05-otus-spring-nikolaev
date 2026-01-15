-- Insert Client Service configuration properties into config_properties table
-- Application name: client-service
-- Profile: default (unless specified otherwise)
-- Label: master (default)

-- app.security.internal-api-key (default value)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.security.internal-api-key', 'some-secret-internal-api-key', 'Internal API key for service-to-service communication (default value)');

-- app.security.auth-service-internal-url
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.security.auth-service-internal-url', 'http://localhost:8081', 'Internal URL for auth service');

-- app.security.auth-service-lb-url
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.security.auth-service-lb-url', 'lb://auth-service', 'Load-balanced URL for auth service');

-- app.security.access-token-max-age
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.security.access-token-max-age', '3600', 'Access token maximum age in seconds');

-- app.security.refresh-token-max-age
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.security.refresh-token-max-age', '43000', 'Refresh token maximum age in seconds');

-- app.security.access-token-expiration-threshold
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.security.access-token-expiration-threshold', '300', 'Threshold for access token expiration in seconds');

-- app.s3.endpoint (default value from -)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.s3.endpoint', 'http://localhost:9000', 'S3 endpoint URL (default value)');

-- app.s3.access-key (default value)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.s3.access-key', 'some-access-s3-key', 'S3 access key (default value)');

-- app.s3.secret-key (default value)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.s3.secret-key', 'some-secret-s3-key', 'S3 secret key (default value)');

-- app.s3.bucket-name (default value from -)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.s3.bucket-name', 'price-files', 'S3 bucket name (default value)');

-- app.s3.region (default value from -)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.s3.region', 'ru-moscow-1', 'S3 region (default value)');

-- app.file.max-file-size
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.file.max-file-size', '20971520', 'Maximum file size in bytes (20MB)');

-- app.file.allowed-extensions
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'app.file.allowed-extensions', '.xlsx, .xls, .csv, .json, .xml, .txt', 'Allowed file extensions for upload');

-- server.compression.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'server.compression.enabled', 'true', 'Enable server response compression');

-- server.compression.min-response-size
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'server.compression.min-response-size', '2KB', 'Minimum response size for compression');

-- server.error.whitelabel.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'server.error.whitelabel.enabled', 'true', 'Enable whitelabel error page');

-- server.ssl.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'server.ssl.enabled', 'false', 'Enable SSL for server');

-- spring.security.oauth2.resourceserver.jwt.secret-key (default value)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.security.oauth2.resourceserver.jwt.secret-key', 'some-secret-jwt-key', 'JWT secret key for token validation (default value)');

-- spring.datasource.driver-class-name
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.datasource.driver-class-name', 'org.postgresql.Driver', 'JDBC driver class name');

-- spring.jpa.generate-ddl
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.jpa.generate-ddl', 'false', 'Disable DDL generation by JPA');

-- spring.jpa.hibernate.ddl-auto
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.jpa.hibernate.ddl-auto', 'validate', 'Hibernate DDL auto mode');

-- spring.jpa.properties.hibernate.format_sql
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.jpa.properties.hibernate.format_sql', 'true', 'Format SQL queries for logging');

-- spring.jpa.show-sql
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.jpa.show-sql', 'false', 'Show SQL queries in logs');

-- spring.jpa.open-in-view
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.jpa.open-in-view', 'true', 'Enable Open Session in View pattern');

-- spring.flyway.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.flyway.enabled', 'true', 'Enable Flyway migrations');

-- spring.flyway.baseline-on-migrate
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.flyway.baseline-on-migrate', 'true', 'Baseline on migrate if needed');

-- spring.flyway.validate-on-migrate
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.flyway.validate-on-migrate', 'true', 'Validate on migrate');

-- spring.flyway.locations
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.flyway.locations', 'classpath:db/migration', 'Locations for Flyway migrations');

-- spring.cloud.config.enabled (changed to true to enable config server)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.cloud.config.enabled', 'true', 'Enable Spring Cloud Config client');

-- spring.cloud.config.uri
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.cloud.config.uri', 'http://localhost:8888', 'Spring Cloud Config server URI');

-- spring.cloud.config.fail-fast
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.cloud.config.fail-fast', 'true', 'Fail fast if config server is not available');

-- spring.cloud.config.retry.initial-interval
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.cloud.config.retry.initial-interval', '5000', 'Initial retry interval for config server connection');

-- spring.cloud.config.retry.max-attempts
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.cloud.config.retry.max-attempts', '3', 'Maximum retry attempts for config server connection');

-- spring.cloud.config.retry.max-interval
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.cloud.config.retry.max-interval', '5000', 'Maximum retry interval for config server connection');

-- spring.cloud.config.retry.multiplier
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.cloud.config.retry.multiplier', '1.2', 'Retry interval multiplier');

-- spring.config.import
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.config.import', 'optional:configserver:http://localhost:8888', 'Import configuration from Spring Cloud Config server');

-- spring.mvc.hiddenmethod.filter.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.mvc.hiddenmethod.filter.enabled', 'true', 'Enable hidden method filter for HTML forms');

-- spring.thymeleaf.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.thymeleaf.enabled', 'true', 'Enable Thymeleaf template engine');

-- spring.thymeleaf.cache
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.thymeleaf.cache', 'false', 'Disable Thymeleaf template caching (dev)');

-- spring.thymeleaf.mode
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.thymeleaf.mode', 'HTML', 'Thymeleaf template mode');

-- spring.thymeleaf.encoding
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.thymeleaf.encoding', 'UTF-8', 'Thymeleaf template encoding');

-- spring.web.resources.chain.cache
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.web.resources.chain.cache', 'false', 'Disable resource chain caching (dev)');

-- spring.web.resources.static-locations
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'spring.web.resources.static-locations', 'classpath:/static/, classpath:/public/, classpath:/resources/, classpath:/META-INF/resources/', 'Static resource locations');

-- eureka.client.service-url.defaultZone
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/', 'Eureka server URL');

-- eureka.client.register-with-eureka
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'eureka.client.register-with-eureka', 'true', 'Register with Eureka server');

-- eureka.client.fetch-registry
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'eureka.client.fetch-registry', 'true', 'Fetch Eureka registry');

-- eureka.instance.prefer-ip-address
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'eureka.instance.prefer-ip-address', 'false', 'Prefer IP address over hostname for Eureka instance');

-- eureka.instance.hostname
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'eureka.instance.hostname', 'localhost', 'Hostname for Eureka instance');

-- resilience4j.retry.configs.default.max-attempts
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.retry.configs.default.max-attempts', '3', 'Default max retry attempts');

-- resilience4j.retry.configs.default.wait-duration
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.retry.configs.default.wait-duration', '1s', 'Default wait duration between retries');

-- resilience4j.retry.configs.default.enable-exponential-backoff
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.retry.configs.default.enable-exponential-backoff', 'true', 'Enable exponential backoff for retries');

-- resilience4j.retry.configs.default.exponential-backoff-multiplier
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.retry.configs.default.exponential-backoff-multiplier', '2', 'Exponential backoff multiplier');

-- resilience4j.retry.instances.auth-service.base-config
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.retry.instances.auth-service.base-config', 'default', 'Base config for auth-service retry');

-- resilience4j.circuitbreaker.configs.default.sliding-window-size
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.circuitbreaker.configs.default.sliding-window-size', '50', 'Sliding window size for circuit breaker');

-- resilience4j.circuitbreaker.configs.default.failure-rate-threshold
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.circuitbreaker.configs.default.failure-rate-threshold', '50', 'Failure rate threshold for circuit breaker');

-- resilience4j.circuitbreaker.configs.default.wait-duration-in-open-state
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.circuitbreaker.configs.default.wait-duration-in-open-state', '30s', 'Wait duration in open state for circuit breaker');

-- resilience4j.circuitbreaker.instances.auth-service.base-config
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.circuitbreaker.instances.auth-service.base-config', 'default', 'Base config for auth-service circuit breaker');

-- resilience4j.ratelimiter.configs.default.limit-for-period
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.ratelimiter.configs.default.limit-for-period', '100', 'Default rate limit per period');

-- resilience4j.ratelimiter.configs.default.limit-refresh-period
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.ratelimiter.configs.default.limit-refresh-period', '1s', 'Default rate limit refresh period');

-- resilience4j.ratelimiter.instances.auth-service.base-config
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'resilience4j.ratelimiter.instances.auth-service.base-config', 'default', 'Base config for auth-service rate limiter');

-- management.endpoints.access.default
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'management.endpoints.access.default', 'read_only', 'Default access level for management endpoints');

-- management.endpoints.web.exposure.include
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'management.endpoints.web.exposure.include', 'health, info, metrics, logfile', 'Exposed management endpoints');

-- management.endpoints.web.base-path
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'management.endpoints.web.base-path', '/actuator', 'Base path for management endpoints');

-- management.endpoint.health.show-details
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'management.endpoint.health.show-details', 'always', 'Show health details');

-- management.endpoint.health.show-components
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'management.endpoint.health.show-components', 'always', 'Show health components');

-- management.endpoint.logfile.access
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'management.endpoint.logfile.access', 'read_only', 'Access level for logfile endpoint');

-- management.health.defaults.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'management.health.defaults.enabled', 'true', 'Enable default health indicators');

-- springdoc.swagger-ui.path
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.swagger-ui.path', '/swagger-ui.html', 'Swagger UI path');

-- springdoc.swagger-ui.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.swagger-ui.enabled', 'true', 'Enable Swagger UI');

-- springdoc.swagger-ui.config-url
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.swagger-ui.config-url', '/v3/api-docs/swagger-config', 'Swagger config URL');

-- springdoc.swagger-ui.url
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.swagger-ui.url', '/v3/api-docs', 'Swagger API docs URL');

-- springdoc.swagger-ui.operations-sorter
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.swagger-ui.operations-sorter', 'method', 'Sort operations by method');

-- springdoc.swagger-ui.tags-sorter
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.swagger-ui.tags-sorter', 'alpha', 'Sort tags alphabetically');

-- springdoc.swagger-ui.display-request-duration
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.swagger-ui.display-request-duration', 'true', 'Display request duration in Swagger UI');

-- springdoc.api-docs.path
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.api-docs.path', '/v3/api-docs', 'API docs path');

-- springdoc.api-docs.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.api-docs.enabled', 'true', 'Enable API docs');

-- springdoc.show-actuator
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.show-actuator', 'true', 'Show actuator endpoints in OpenAPI docs');

-- springdoc.packages-to-scan
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.packages-to-scan', 'ru.pricat.controller.api', 'Packages to scan for OpenAPI documentation');

-- springdoc.default-produces-media-type
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'springdoc.default-produces-media-type', 'application/json', 'Default media type for responses');

-- logging.file.name
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.file.name', './logs/client-service.log', 'Log file name');

-- logging.config
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.config', 'classpath:logback.xml', 'Logback configuration file location');

-- logging.level.root
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.level.root', 'INFO', 'Root log level');

-- logging.level.ru.pricat
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.level.ru.pricat', 'INFO', 'Log level for ru.pricat package');

-- logging.level.org.flywaydb
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.level.org.flywaydb', 'INFO', 'Log level for org.flywaydb package');

-- logging.level.org.thymeleaf
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.level.org.thymeleaf', 'INFO', 'Log level for org.thymeleaf package');

-- logging.level.springframework.orm.jpa
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.level.springframework.orm.jpa', 'WARN', 'Log level for springframework.orm.jpa package');

-- logging.level.org.springframework.web
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.level.org.springframework.web', 'DEBUG', 'Log level for org.springframework.web package');

-- logging.level.org.springframework.web.HttpLogging
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.level.org.springframework.web.HttpLogging', 'INFO', 'Log level for HttpLogging');

-- logging.level.org.springframework.security
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.level.org.springframework.security', 'DEBUG', 'Log level for org.springframework.security package');

-- logging.level.org.springframework.cloud.config
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.level.org.springframework.cloud.config', 'INFO', 'Log level for org.springframework.cloud.config package');

-- logging.level.org.springframework.web.client.RestTemplate
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.level.org.springframework.web.client.RestTemplate', 'INFO', 'Log level for RestTemplate');

-- logging.level.com.netflix.discovery.shared.resolver.aws.ConfigClusterResolver
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('client-service', 'default', 'master', 'logging.level.com.netflix.discovery.shared.resolver.aws.ConfigClusterResolver', 'WARN', 'Log level for ConfigClusterResolver');