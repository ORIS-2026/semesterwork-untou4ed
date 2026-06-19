package ru.itis.services.minio;

import ru.itis.exceptions.MinioException;
import ru.itis.properties.MinioProperties;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String buildAvatarObjectKey(UUID userId) {
        return "avatars/%s".formatted(userId);
    }

    public Map<String, String> generateUploadFormData(String objectKey, int ttlMinutes, long maxSizeBytes) {
        try {
            ensureBucketExists();
            PostPolicy policy = new PostPolicy(minioProperties.getBucket(), ZonedDateTime.now().plusMinutes(ttlMinutes));
            policy.addEqualsCondition("key", objectKey);
            policy.addStartsWithCondition("Content-Type", "image/");
            policy.addContentLengthRangeCondition(1, maxSizeBytes);

            Map<String, String> formData = new HashMap<>(minioClient.getPresignedPostFormData(policy));
            formData.put("key", objectKey);
            return formData;
        } catch (Exception e) {
            throw new MinioException("Ошибка создания upload form data", e);
        }
    }

    public String generateDownloadPresignedUrl(String objectKey, int ttlMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .expiry(ttlMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            throw new MinioException("Ошибка создания presigned URL для скачивания", e);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .build());
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .build());
        }
    }
}
