-- Insert Auth Service configuration properties into config_properties table
-- Application name: auth-service
-- Profile: default (unless specified otherwise)
-- Label: master (default)

-- app.security.refresh-token-max-age
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'app.security.refresh-token-max-age', '43000', 'Refresh token maximum age in seconds');

-- app.security.refresh-token-cleanup-time
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'app.security.refresh-token-cleanup-time', '0 0 2 * * ?', 'Cron expression for refresh token cleanup task');

-- app.security.max-login-attempts
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'app.security.max-login-attempts', '3', 'Maximum number of login attempts before lockout');

-- app.security.token-blacklist-ttl-minutes
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'app.security.token-blacklist-ttl-minutes', '65', 'Time-to-live for blacklisted tokens in minutes');

-- app.security.token-blacklist-max-size
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'app.security.token-blacklist-max-size', '10000', 'Maximum size of token blacklist cache');

-- app.security.client-service-base-url
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'app.security.client-service-base-url', 'http://localhost:8083', 'Base URL for client service API calls');

-- server.compression.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'server.compression.enabled', 'true', 'Enable server response compression');

-- server.compression.min-response-size
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'server.compression.min-response-size', '2KB', 'Minimum response size for compression');

-- server.error.whitelabel.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'server.error.whitelabel.enabled', 'true', 'Enable whitelabel error page');

-- server.ssl.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'server.ssl.enabled', 'false', 'Enable SSL for server');

-- spring.banner.charset
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.banner.charset', 'UTF-8', 'Charset for banner file');

-- spring.r2dbc.url (this will be overridden by env var if present)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.r2dbc.url', 'r2dbc:postgresql://localhost:5433/auth_db', 'R2DBC URL for database connection (default value)');

-- spring.r2dbc.username (this will be overridden by env var if present)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.r2dbc.username', 'auth_user', 'R2DBC username (default value)');

-- spring.datasource.url (this will be overridden by env var if present)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.datasource.url', 'jdbc:postgresql://localhost:5433/auth_db', 'JDBC URL for database connection (default value)');

-- spring.datasource.username (this will be overridden by env var if present)
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.datasource.username', 'auth_user', 'JDBC username (default value)');

-- spring.datasource.driver-class-name
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.datasource.driver-class-name', 'org.postgresql.Driver', 'JDBC driver class name');

-- spring.flyway.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.flyway.enabled', 'true', 'Enable Flyway migrations');

-- spring.flyway.baseline-on-migrate
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.flyway.baseline-on-migrate', 'true', 'Baseline on migrate if needed');

-- spring.flyway.validate-on-migrate
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.flyway.validate-on-migrate', 'true', 'Validate on migrate');

-- spring.flyway.locations
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.flyway.locations', 'classpath:db/migration', 'Locations for Flyway migrations');

-- spring.cloud.config.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.cloud.config.enabled', 'true', 'Enable Spring Cloud Config client');

-- spring.cloud.config.uri
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.cloud.config.uri', 'http://localhost:8888', 'Spring Cloud Config server URI');

-- spring.cloud.config.fail-fast
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.cloud.config.fail-fast', 'true', 'Fail fast if config server is not available');

-- spring.cloud.config.retry.initial-interval
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.cloud.config.retry.initial-interval', '5000', 'Initial retry interval for config server connection');

-- spring.cloud.config.retry.max-attempts
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.cloud.config.retry.max-attempts', '3', 'Maximum retry attempts for config server connection');

-- spring.cloud.config.retry.max-interval
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.cloud.config.retry.max-interval', '5000', 'Maximum retry interval for config server connection');

-- spring.cloud.config.retry.multiplier
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.cloud.config.retry.multiplier', '1.2', 'Retry interval multiplier');

-- spring.config.import
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.config.import', 'optional:configserver:http://localhost:8888', 'Import configuration from Spring Cloud Config server');

-- spring.cache.type
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.cache.type', 'caffeine', 'Type of cache to use');

-- spring.cache.caffeine.spec
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'spring.cache.caffeine.spec', 'initialCapacity=1000,maximumSize=10000,expireAfterWrite=10m', 'Caffeine cache specification');

-- eureka.client.service-url.defaultZone
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/', 'Eureka server URL');

-- eureka.client.register-with-eureka
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'eureka.client.register-with-eureka', 'true', 'Register with Eureka server');

-- eureka.client.fetch-registry
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'eureka.client.fetch-registry', 'true', 'Fetch Eureka registry');

-- eureka.instance.prefer-ip-address
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'eureka.instance.prefer-ip-address', 'false', 'Prefer IP address over hostname for Eureka instance');

-- eureka.instance.hostname
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'eureka.instance.hostname', 'localhost', 'Hostname for Eureka instance');

-- management.endpoints.access.default
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'management.endpoints.access.default', 'read_only', 'Default access level for management endpoints');

-- management.endpoints.web.exposure.include
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'management.endpoints.web.exposure.include', 'health, info, metrics, logfile', 'Exposed management endpoints');

-- management.endpoints.web.base-path
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'management.endpoints.web.base-path', '/actuator', 'Base path for management endpoints');

-- management.endpoint.health.show-details
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'management.endpoint.health.show-details', 'always', 'Show health details');

-- management.endpoint.health.show-components
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'management.endpoint.health.show-components', 'always', 'Show health components');

-- management.endpoint.logfile.access
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'management.endpoint.logfile.access', 'read_only', 'Access level for logfile endpoint');

-- management.endpoint.info.access
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'management.endpoint.info.access', 'read_only', 'Access level for info endpoint');

-- management.health.defaults.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'management.health.defaults.enabled', 'true', 'Enable default health indicators');

-- springdoc.swagger-ui.path
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.swagger-ui.path', '/swagger-ui.html', 'Swagger UI path');

-- springdoc.swagger-ui.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.swagger-ui.enabled', 'true', 'Enable Swagger UI');

-- springdoc.swagger-ui.config-url
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.swagger-ui.config-url', '/v3/api-docs/swagger-config', 'Swagger config URL');

-- springdoc.swagger-ui.url
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.swagger-ui.url', '/v3/api-docs', 'Swagger API docs URL');

-- springdoc.swagger-ui.operations-sorter
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.swagger-ui.operations-sorter', 'method', 'Sort operations by method');

-- springdoc.swagger-ui.tags-sorter
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.swagger-ui.tags-sorter', 'alpha', 'Sort tags alphabetically');

-- springdoc.swagger-ui.display-request-duration
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.swagger-ui.display-request-duration', 'true', 'Display request duration in Swagger UI');

-- springdoc.api-docs.path
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.api-docs.path', '/v3/api-docs', 'API docs path');

-- springdoc.api-docs.enabled
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.api-docs.enabled', 'true', 'Enable API docs');

-- springdoc.show-actuator
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.show-actuator', 'true', 'Show actuator endpoints in OpenAPI docs');

-- springdoc.packages-to-scan
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.packages-to-scan', 'ru.pricat.controller', 'Packages to scan for OpenAPI documentation');

-- springdoc.default-produces-media-type
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'springdoc.default-produces-media-type', 'application/json', 'Default media type for responses');

-- logging.file.name
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.file.name', './logs/auth-service.log', 'Log file name');

-- logging.config
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.config', 'classpath:logback.xml', 'Logback configuration file location');

-- logging.level.ru.pricat
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.level.ru.pricat', 'DEBUG', 'Log level for ru.pricat package');

-- logging.level.io.r2dbc
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.level.io.r2dbc', 'WARN', 'Log level for io.r2dbc package');

-- logging.level.org.flywaydb
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.level.org.flywaydb', 'WARN', 'Log level for org.flywaydb package');

-- logging.level.org.springframework.web
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.level.org.springframework.web', 'DEBUG', 'Log level for org.springframework.web package');

-- logging.level.org.springframework.web.HttpLogging
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.level.org.springframework.web.HttpLogging', 'INFO', 'Log level for HttpLogging');

-- logging.level.org.springframework.jdbc
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.level.org.springframework.jdbc', 'WARN', 'Log level for org.springframework.jdbc package');

-- logging.level.org.springframework.security
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.level.org.springframework.security', 'DEBUG', 'Log level for org.springframework.security package');

-- logging.level.org.springframework.cloud.config
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.level.org.springframework.cloud.config', 'WARN', 'Log level for org.springframework.cloud.config package');

-- logging.level.org.springframework.r2dbc
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.level.org.springframework.r2dbc', 'WARN', 'Log level for org.springframework.r2dbc package');

-- logging.level.org.springframework.web.client.RestTemplate
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.level.org.springframework.web.client.RestTemplate', 'INFO', 'Log level for RestTemplate');

-- logging.level.com.netflix.discovery.shared.resolver.aws.ConfigClusterResolver
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description)
VALUES ('auth-service', 'default', 'master', 'logging.level.com.netflix.discovery.shared.resolver.aws.ConfigClusterResolver', 'WARN', 'Log level for ConfigClusterResolver');