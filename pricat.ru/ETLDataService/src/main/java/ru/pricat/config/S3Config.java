package ru.pricat.config;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Конфигурация для S3 хранилища.
 */
@Slf4j
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class S3Config {

    final S3Properties s3Properties;

    public S3Config(S3Properties s3properties) {
        this.s3Properties = s3properties;
    }

    /**
     * Создает и настраивает S3Client для работы с MinIO.
     *
     * <p><b>Важные моменты:</b>
     * <ul>
     *   <li>endpointOverride: указывает на MinIO вместо AWS S3</li>
     *   <li>pathStyleAccessEnabled: true для совместимости с MinIO</li>
     *   <li>StaticCredentialsProvider: использование логина/пароля MinIO</li>
     * </ul>
     *
     * @return настроенный S3Client instance
     * @throws IllegalArgumentException если endpoint невалидный
     */
    @Bean
    public S3Client s3Client() {
        try {
            return S3Client.builder()
                    .endpointOverride(URI.create(s3Properties.getEndpoint()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(s3Properties.getAccessKey(), s3Properties.getSecretKey())))
                    .region(Region.of(s3Properties.getRegion()))
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                    .build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid S3 endpoint configuration: {}", e.getMessage());
            throw e;
        }
    }
}

