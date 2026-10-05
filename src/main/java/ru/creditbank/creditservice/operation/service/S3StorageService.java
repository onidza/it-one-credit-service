package ru.creditbank.creditservice.operation.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${s3.bucket}")
  private String bucketName;

  @Value("${file.presigned-url-ttl}")
  private Duration ttl;

  public ResponseInputStream<GetObjectResponse> download(String objectKey) {
    log.info("Download from S3: key = {}", objectKey);

    GetObjectRequest request = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(objectKey)
        .build();

    return s3Client.getObject(request);
  }

  public PresignedGetObjectRequest generatePresignedUrl(
      String objectKey
  ) {
    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(ttl)
            .getObjectRequest(request -> request
                .bucket(bucketName)
                .key(objectKey)
            )
            .build();

    return s3Presigner.presignGetObject(presignRequest);
  }
}
