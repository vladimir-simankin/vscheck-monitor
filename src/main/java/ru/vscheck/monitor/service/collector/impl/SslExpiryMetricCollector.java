package ru.vscheck.monitor.service.collector.impl;

import java.net.URL;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vscheck.monitor.service.collector.MetricCollector;
import ru.vscheck.proto.metrics.Metric;
import ru.vscheck.proto.metrics.MetricType;
import ru.vscheck.proto.request.MetricRequest;

@Service
@RequiredArgsConstructor
public class SslExpiryMetricCollector implements MetricCollector {

  @Override
  public MetricType getSupportedType() {
    return MetricType.SSL_EXPIRY;
  }

  @Override
  public Metric collect(MetricRequest metricRequest) {
    try {
      URL url = new URL(metricRequest.getTarget());
      String host = url.getHost();
      int port = url.getPort() == -1 ? 443 : url.getPort();

      SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
        socket.startHandshake();

        X509Certificate cert = (X509Certificate) socket.getSession().getPeerCertificates()[0];
        Instant notAfter = cert.getNotAfter().toInstant();
        long daysUntilExpiry = ChronoUnit.DAYS.between(Instant.now(), notAfter);
        boolean certValid = Instant.now().isBefore(notAfter);

        return Metric.newBuilder()
            .setCheckId(metricRequest.getCheckId())
            .setMetricType(MetricType.SSL_EXPIRY)
            .setTargetType(metricRequest.getTargetType())
            .setTarget(metricRequest.getTarget())
            .setSuccess(true)
            .setCertValid(certValid)
            .setSslDaysLeft((int) daysUntilExpiry)
            .setTimestamp(System.currentTimeMillis())
            .putExtra("not_after", notAfter.toString())
            .putExtra("issuer", cert.getIssuerX500Principal().getName())
            .build();
      }

    } catch (SSLPeerUnverifiedException e) {
      return handleException(metricRequest, "ssl_unverified", e.getMessage());
    } catch (Exception e) {
      return handleException(metricRequest, "ssl_error", e.getMessage());
    }
  }

  private Metric handleException(MetricRequest request, String failureReason, String message) {
    return Metric.newBuilder()
        .setMetricType(MetricType.SSL_EXPIRY)
        .setTargetType(request.getTargetType())
        .setTarget(request.getTarget())
        .setSuccess(false)
        .setFailureReason(failureReason)
        .setMessage(message)
        .setTimestamp(System.currentTimeMillis())
        .build();
  }
}
