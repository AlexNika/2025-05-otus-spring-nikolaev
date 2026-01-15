package ru.pricat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ExposureConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import ru.pricat.model.entity.Company;

/**
 * Конфигурация для Data Rest.
 */
@Configuration
public class RestConfig implements RepositoryRestConfigurer {


    /**
     * Метод переопределяет стандартную конфигурацию Data Rest и выключает POST, PUT, PATCH и DELETE методы
     *
     * @param restConfig стандартный restConfig
     * @param cors       стандартный cors
     */
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration restConfig,
                                                     CorsRegistry cors) {
        ExposureConfiguration config = restConfig.getExposureConfiguration();
        config.forDomainType(Company.class).withItemExposure((_, httpMethods) ->
                httpMethods.disable(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE));
    }

}
