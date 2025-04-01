package ru.vscheck.monitor.test;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetricResultJsonDTO {

  private String checkId;
  private String target;
  private String targetType;
  private String metricType;

  private Long timestamp;
  private String location;

  private Boolean success;
  private String failureReason;
  private String message;

  private Long responseTimeMs;
  private Integer timeoutMs;

  private Boolean portOpen;
  private Boolean icmpReachable;
  private Boolean dnsResolved;

  private Boolean httpAvailable;
  private Integer httpStatus;
  private Boolean keywordFound;
  private Map<String, String> headers;

  private Integer sslDaysLeft;
  private Boolean certValid;
  private Integer domainDaysLeft;
  private String whoisExpiryDate;

  private Map<String, String> extra;

  public static MetricResultJsonDTO fromProto(ru.vscheck.proto.metrics.Metric metric) {
    return MetricResultJsonDTO.builder()
        .checkId(metric.getCheckId())
        .target(metric.getTarget())
        .targetType(metric.getTargetType().name())
        .metricType(metric.getMetricType().name())
        .timestamp(metric.getTimestamp())
        .location(metric.getLocation())
        .success(metric.getSuccess())
        .failureReason(metric.getFailureReason())
        .message(metric.getMessage())
        .responseTimeMs(metric.getResponseTimeMs())
        .timeoutMs(metric.getTimeoutMs())
        .portOpen(metric.getPortOpen())
        .icmpReachable(metric.getIcmpReachable())
        .dnsResolved(metric.getDnsResolved())
        .httpAvailable(metric.getHttpAvailable())
        .httpStatus(metric.getHttpStatus())
        .keywordFound(metric.getKeywordFound())
        .headers(metric.getHeadersMap())
        .sslDaysLeft(metric.getSslDaysLeft())
        .certValid(metric.getCertValid())
        .domainDaysLeft(metric.getDomainDaysLeft())
        .whoisExpiryDate(metric.getWhoisExpiryDate())
        .extra(metric.getExtraMap())
        .build();
  }
}
