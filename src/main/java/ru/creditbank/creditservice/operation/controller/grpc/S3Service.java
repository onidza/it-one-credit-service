package ru.creditbank.creditservice.operation.controller.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.creditbank.contract.s3.v1.DownloadFileChunk;
import ru.creditbank.contract.s3.v1.DownloadFileRequest;
import ru.creditbank.contract.s3.v1.FileServiceGrpc.FileServiceImplBase;
import ru.creditbank.contract.s3.v1.GenerateUrlRequest;
import ru.creditbank.contract.s3.v1.GenerateUrlResponse;
import ru.creditbank.creditservice.operation.service.S3StorageService;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service extends FileServiceImplBase {

  private final S3StorageService s3StorageService;

  @Override
  public void downloadFile(
      DownloadFileRequest request,
      StreamObserver<DownloadFileChunk> clientObserver
  ) {
    log.info("Download from S3: key = {}", request.getFileId());

    try (InputStream inputStream =
        s3StorageService.download(
            request.getFileId()
        )
    ) {
      byte[] buffer = new byte[64 * 1024];
      int bytesRead;

      while ((bytesRead = inputStream.read(buffer)) != -1) {
        DownloadFileChunk chunk = DownloadFileChunk.newBuilder()
            .setData(ByteString.copyFrom(
                    buffer, 0, bytesRead
                )
            )
            .build();

        clientObserver.onNext(chunk);
      }

      clientObserver.onCompleted();

    } catch (Exception e) {
      log.error(
          "Failed to download file from S3, key = {}",
          request.getFileId(),
          e
      );

      clientObserver.onError(
          Status.INTERNAL
              .withDescription("Failed to download file")
              .withCause(e)
              .asRuntimeException()
      );
    }
  }

  @Override
  public void generatePresignedUrl(
      GenerateUrlRequest request,
      StreamObserver<GenerateUrlResponse> clientObserver
  ) {
    PresignedGetObjectRequest presignedUrl =
        s3StorageService.generatePresignedUrl(request.getFileId());
    Instant expiration = presignedUrl.expiration();

    GenerateUrlResponse response = GenerateUrlResponse.newBuilder()
        .setUrl(presignedUrl.url().toString())
        .setExpiresAt(Timestamp.newBuilder()
            .setSeconds(expiration.getEpochSecond())
            .setNanos(expiration.getNano())
            .build()
        )
        .build();

    clientObserver.onNext(response);
    clientObserver.onCompleted();
  }
}
