package ru.creditbank.creditservice.operation.controller.grpc;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.creditbank.contract.test_interactions.v1.DownloadRequest;
import ru.creditbank.contract.test_interactions.v1.FileChunk;
import ru.creditbank.contract.test_interactions.v1.TestInteractionsServiceGrpc;
import ru.creditbank.contract.test_interactions.v1.TestRequest;
import ru.creditbank.contract.test_interactions.v1.TestResponse;
import ru.creditbank.contract.test_interactions.v1.TransferProgress;
import ru.creditbank.contract.test_interactions.v1.UploadResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestIntegrationServiceGrpc extends
    TestInteractionsServiceGrpc.TestInteractionsServiceImplBase {

  @Override
  public void unaryTest(TestRequest request, StreamObserver<TestResponse> clientObserver) {
    clientObserver.onNext(TestResponse
        .newBuilder()
        .setMessage("Test answer from server. Got ur message: %s "
            .formatted(request.getMessage()))
        .build()
    );

    clientObserver.onCompleted();
  }

  @Override
  public StreamObserver<FileChunk> uploadFile(StreamObserver<UploadResult> clientObserver) {
    return new StreamObserver<>() {

      private long totalBytes = 0;
      private int chunksCount = 0;

      @Override
      public void onNext(FileChunk chunk) {
        totalBytes += chunk.getData().size();
        chunksCount++;

        log.info(
            "Received chunk {}, size = {}",
            chunksCount,
            chunk.getData().size()
        );
      }

      @Override
      public void onError(Throwable t) {
        clientObserver.onError(t);
      }

      @Override
      public void onCompleted() {
        clientObserver.onNext(
            UploadResult.newBuilder()
                .setFileName("Test_file_name")
                .setTotalBytes(totalBytes)
                .setChunksCount(chunksCount)
                .build()
        );

        clientObserver.onCompleted();
      }
    };
  }

  @Override
  public void downloadFile(DownloadRequest request, StreamObserver<FileChunk> clientObserver) {
    clientObserver.onNext(
        FileChunk.newBuilder()
            .setData(ByteString.copyFromUtf8("chunk 1"))
            .setChunkNumber(1)
            .build()
    );

    clientObserver.onNext(
        FileChunk.newBuilder()
            .setData(ByteString.copyFromUtf8("chunk 2"))
            .setChunkNumber(2)
            .build()
    );

    clientObserver.onNext(
        FileChunk.newBuilder()
            .setData(ByteString.copyFromUtf8("chunk 3"))
            .setChunkNumber(3)
            .build()
    );

    clientObserver.onCompleted();
  }

  @Override
  public StreamObserver<FileChunk> transfer(StreamObserver<TransferProgress> clientObserver) {
    return new StreamObserver<>() {

      private long processedBytes = 0;
      private long totalBytes = 0;

      @Override
      public void onNext(FileChunk chunk) {
        if (totalBytes == 0 && chunk.hasTotalBytes()) {
          totalBytes = chunk.getTotalBytes();
        }

        processedBytes += chunk.getData().size();
        int percent = totalBytes > 0 ? (int) (processedBytes * 100.0 / totalBytes) : 0;

        clientObserver.onNext(
            TransferProgress.newBuilder()
                .setBytesProcessed(processedBytes)
                .setTotalBytes(totalBytes)
                .setPercent(percent)
                .build()
        );
      }

      @Override
      public void onError(Throwable t) {
        clientObserver.onError(t);
      }

      @Override
      public void onCompleted() {
        clientObserver.onCompleted();
      }
    };
  }
}
