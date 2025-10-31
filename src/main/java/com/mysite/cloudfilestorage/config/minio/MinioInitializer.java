package com.mysite.cloudfilestorage.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioInitializer {

    private final MinioProperties minioProperties;
    private final MinioClient minioClient;

    @PostConstruct
    public void init() throws Exception {
        String bucketName = minioProperties.getBucket();
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());

        if (!found) {
            createBucket(minioClient);
        }
    }

    public void createBucket(MinioClient minioClient) throws Exception {
        String bucketName = minioProperties.getBucket();
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }
}
