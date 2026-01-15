-- Config Server self-configuration
INSERT INTO config_properties (application, profile, label, prop_key, prop_value, description) VALUES
('config-server', 'default', 'master', 'server.port', '8888', 'Config Server port'),
('config-server', 'default', 'master', 'spring.application.name', 'config-server', 'Config Server spring application name'),
('config-server', 'default', 'master', 'spring.datasource.url', 'jdbc:postgresql://localhost:5432/config_db', 'Config Server database URL'),
('config-server', 'default', 'master', 'spring.datasource.username', 'config_user', 'Config Server database username'),
('config-server', 'default', 'master', 'spring.datasource.password', 'config_pass', 'Config Server database password'),
('config-server', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/', 'Eureka Server URL'),
('config-server', 'default', 'master', 'spring.flyway.enabled', 'true', 'Enable Flyway migrations'),

-- Eureka Server configuration
('eureka-server', 'default', 'master', 'server.port', '8761', 'Eureka Server port'),
('eureka-server', 'default', 'master', 'spring.application.name', 'eureka-server', 'Eureka server spring application name'),
('eureka-server', 'default', 'master', 'eureka.client.register-with-eureka', 'false', 'Eureka Server should not register itself'),
('eureka-server', 'default', 'master', 'eureka.client.fetch-registry', 'false', 'Eureka Server should not fetch registry'),
('eureka-server', 'default', 'master', 'eureka.server.enable-self-preservation', 'false', 'Eureka Server self-preservation mode'),
('eureka-server', 'default', 'master', 'eureka.server.eviction-interval-timer-in-ms', '60000', 'Eureka Server controls the eviction interval'),
('eureka-server', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/', 'Eureka Server URL'),

-- Common configurations for all services
('application', 'default', 'master', 'eureka.client.service-url.defaultZone', 'http://localhost:8761/eureka/', 'Eureka Server URL for all services'),
('application', 'default', 'master', 'spring.cloud.config.uri', 'http://localhost:8888', 'Config Server URI for all services')
ON CONFLICT (application, profile, label, prop_key) DO NOTHING;
