package com.shegami.hr_saas.config.domain.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@Profile("local")
public class MinioConfig {

        @Value("${aws.s3.endpoint}")
        private String endpoint;

        @Value("${aws.s3.access-key}")
        private String accessKey;

        @Value("${aws.s3.secret-key}")
        private String secretKey;

        @Value("${aws.s3.region}")
        private String region;

        @Value("${aws.s3.bucket-name}")
        private String bucketName;

        @Bean
        public S3Client getS3Client() {
                S3Client client = S3Client.builder()
                                .region(Region.of(region))
                                .endpointOverride(URI.create(endpoint))
                                .credentialsProvider(StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create(accessKey, secretKey)))
                                .forcePathStyle(true)
                                .build();

                // Ensure bucket exists at startup
                try {
                        client.createBucket(r -> r.bucket(bucketName));
                } catch (Exception e) {
                        // Ignore if it already exists or if it fails for other reasons (user will see
                        // it later if it's a real issue)
                }

                return client;
        }

        @Bean
        public S3Presigner getS3Presigner() {
                return S3Presigner.builder()
                                .region(Region.of(region))
                                .endpointOverride(URI.create(endpoint))
                                .credentialsProvider(StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create(accessKey, secretKey)))
                                .serviceConfiguration(S3Configuration.builder()
                                                .pathStyleAccessEnabled(true)
                                                .build())
                                .build();
        }
}