package ru.vscheck.monitor.service.collector.impl;

import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.vscheck.monitor.service.collector.MetricCollector;
import ru.vscheck.proto.metrics.Metric;
import ru.vscheck.proto.metrics.MetricType;
import ru.vscheck.proto.request.MetricRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpMetricCollector implements MetricCollector {

  @Value("${http.client.timeout-ms:5000}")
  private int defaultTimeoutMs;

  @Value("${vscheck.rabbit.queues.result}")
  private String resultQueue;

  private final HttpClient httpClient;
  private final RabbitTemplate rabbitTemplate;

  @Override
  public MetricType getSupportedType() {
    return MetricType.HTTP;
  }

  @Override
  public Metric collect(MetricRequest metricRequest) {
    log.info("Collecting http metric {}", metricRequest);

    int timeoutMs =
        metricRequest.getTimeoutMs() > 0 ? metricRequest.getTimeoutMs() : defaultTimeoutMs;

    HttpRequest request = buildRequest(metricRequest, timeoutMs);
    Metric.Builder metric = buildMetric(metricRequest, timeoutMs);

    Instant start = Instant.now();
    Metric result;
    try {
      HttpResponse<Void> response =
          httpClient.send(request, HttpResponse.BodyHandlers.discarding());
      log.info(
          "HTTP check for {} completed success. Status: {}",
          metricRequest.getTarget(),
          metric.getHttpStatus());

      long duration = Duration.between(start, Instant.now()).toMillis();
      metric
          .setSuccess(true)
          .setHttpAvailable(true)
          .setHttpStatus(response.statusCode())
          .setResponseTimeMs(duration);

      result = collectHeaders(response, metric);

    } catch (UnknownHostException e) {
      result = handleException(e, false, "dns_failed", metric, start);
    } catch (ConnectException e) {
      result = handleException(e, false, "connection_refused", metric, start);
    } catch (HttpTimeoutException e) {
      result = handleException(e, true, "timeout", metric, start);
    } catch (SSLException e) {
      result = handleException(e, true, "ssl_error", metric, start);
    } catch (Exception e) {
      result = handleException(e, true, "unknown_error", metric, start);
    }
    //    rabbitTemplate.convertAndSend(resultQueue, metric);
    return result;
  }

  private HttpRequest buildRequest(MetricRequest metricRequest, int timeoutMs) {
    String url = metricRequest.getTarget();

    return HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(Duration.ofMillis(timeoutMs))
        .GET()
        .build();
  }

  private Metric.Builder buildMetric(MetricRequest metricRequest, int timeoutMs) {
    return Metric.newBuilder()
        .setCheckId(metricRequest.getCheckId())
        .setTarget(metricRequest.getTarget())
        .setTargetType(metricRequest.getTargetType())
        .setMetricType(metricRequest.getMetricType())
        .setTimestamp(System.currentTimeMillis())
        .setTimeoutMs(timeoutMs)
        .setLocation(metricRequest.getLocation());
  }

  private Metric collectHeaders(HttpResponse<Void> response, Metric.Builder metric) {
    // Собираем заголовки (если нужно)
    Map<String, String> headerMap = new HashMap<>();
    response
        .headers()
        .map()
        .forEach(
            (k, v) -> {
              if (!v.isEmpty()) {
                headerMap.put(k, String.join(",", v));
              }
            });
    metric.putAllHeaders(headerMap);
    return metric.build();
  }

  private Metric handleException(
      Exception e,
      boolean httpAvailable,
      String failureReason,
      Metric.Builder metric,
      Instant start) {

    log.info("HTTP check completed failed. Status: {}", metric.getHttpStatus());

    long duration = Duration.between(start, Instant.now()).toMillis();

    return metric
        .setSuccess(false)
        .setHttpAvailable(httpAvailable) // Доступность зависит от типа ошибки
        .setResponseTimeMs(duration)
        .setFailureReason(failureReason)
        .setMessage(e.getMessage() != null ? e.getMessage() : e.toString())
        .build();
  }
}
