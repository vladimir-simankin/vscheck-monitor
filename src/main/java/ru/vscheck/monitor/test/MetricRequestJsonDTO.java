package ru.vscheck.monitor.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.vscheck.proto.metrics.MetricType;
import ru.vscheck.proto.metrics.TargetType;
import ru.vscheck.proto.request.MetricRequest;

import java.util.Map;

@Data
public class MetricRequestJsonDTO {
  private String checkId;
  private String target;
  private String targetType;  // DOMAIN, IP, URL
  private String metricType;  // HTTP, HTTP_KEYWORD, SSL_EXPIRY и т.д.
  private String location;
  private Integer timeoutMs;

  // Специфичные поля
  private String expectedKeyword;              // для HTTP_KEYWORD
  private Map<String, String> expectedHeaders; // для HTTP_HEADERS
  private Integer port;                        // для TCP_PORT

  public ru.vscheck.proto.request.MetricRequest toProto() {
    var builder = ru.vscheck.proto.request.MetricRequest.newBuilder()
            .setCheckId(checkId)
            .setTarget(target)
            .setTargetType(ru.vscheck.proto.metrics.TargetType.valueOf(targetType))
            .setMetricType(ru.vscheck.proto.metrics.MetricType.valueOf(metricType))
            .setLocation(location)
            .setTimeoutMs(timeoutMs != null ? timeoutMs : 0);

    if (expectedKeyword != null) {
      builder.setExpectedKeyword(expectedKeyword);
    }

    if (expectedHeaders != null) {
      builder.putAllExpectedHeaders(expectedHeaders);
    }

    if (port != null) {
      builder.setPort(port);
    }

    return builder.build();
  }
}
