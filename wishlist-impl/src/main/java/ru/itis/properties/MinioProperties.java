package ru.itis.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String publicUrl;
    private int avatarUploadTtlMinutes = 15;
    private int avatarDownloadTtlMinutes = 60;
    private long avatarMaxSizeBytes = 5 * 1024 * 1024L;
}
