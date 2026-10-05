package ru.creditbank.creditservice.config.filter.grpc;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

public class TraceIdServerInterceptor implements ServerInterceptor {

  private static final Metadata.Key<String> TRACE_ID =
      Metadata.Key.of(
          "x-trace-id",
          Metadata.ASCII_STRING_MARSHALLER
      );

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      Metadata headers,
      ServerCallHandler<ReqT, RespT> next
  ) {
    String traceId = headers.get(TRACE_ID);

    if (traceId == null || traceId.isBlank()) {
      traceId = UUID.randomUUID().toString();
    }

    String finalTraceId = traceId;

    ServerCall.Listener<ReqT> listener =
        next.startCall(call, headers);

    return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
        listener
    ) {

      @Override
      public void onMessage(ReqT message) {
        withMdc(() -> super.onMessage(message));
      }

      @Override
      public void onHalfClose() {
        withMdc(super::onHalfClose);
      }

      @Override
      public void onCancel() {
        withMdc(super::onCancel);
      }

      @Override
      public void onComplete() {
        withMdc(super::onComplete);
      }

      @Override
      public void onReady() {
        withMdc(super::onReady);
      }

      private void withMdc(Runnable action) {
        try {
          MDC.put("traceId", finalTraceId);
          action.run();
        } finally {
          MDC.clear();
        }
      }
    };
  }
}
