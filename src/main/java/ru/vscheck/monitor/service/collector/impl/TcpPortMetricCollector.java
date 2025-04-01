package ru.vscheck.monitor.service.collector.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vscheck.monitor.service.collector.MetricCollector;
import ru.vscheck.proto.metrics.Metric;
import ru.vscheck.proto.metrics.MetricType;
import ru.vscheck.proto.metrics.TargetType;
import ru.vscheck.proto.request.MetricRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcpPortMetricCollector implements MetricCollector {

  private static final int TIMEOUT_MILLIS = 5000;

  @Override
  public MetricType getSupportedType() {
    return MetricType.TCP_PORT;
  }

  @Override
  public Metric collect(MetricRequest request) {
    String target = request.getTarget();
    int port = request.getPort() == 0 ? 443 : request.getPort();
    String checkId = request.getCheckId(); // предположим, он есть в MetricRequest
    String location = request.getLocation(); // если есть (иначе "")

    long start = System.currentTimeMillis();
    boolean portOpen = false;
    boolean success = false;
    String failureReason = "";
    String message = "";

    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(target, port), TIMEOUT_MILLIS);
      portOpen = true;
      success = true;
    } catch (SocketTimeoutException e) {
      failureReason = "timeout";
      message = e.getMessage();
    } catch (IOException e) {
      failureReason = "connection_failed";
      message = e.getMessage();
    }

    long responseTime = System.currentTimeMillis() - start;

    return Metric.newBuilder()
        .setCheckId(checkId)
        .setTarget(target)
        .setTargetType(resolveTargetType(target))
        .setMetricType(MetricType.TCP_PORT)
        .setTimestamp(System.currentTimeMillis())
        .setLocation(location)
        .setSuccess(success)
        .setFailureReason(failureReason)
        .setMessage(message)
        .setResponseTimeMs(responseTime)
        .setTimeoutMs(TIMEOUT_MILLIS)
        .setPortOpen(portOpen)
        .build();
  }

  private TargetType resolveTargetType(String target) {
    if (target == null || target.isEmpty()) {
      return TargetType.UNKNOWN_TARGET;
    }
    if (target.matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
      return TargetType.IP;
    }
    if (target.contains(".")) {
      return TargetType.DOMAIN;
    }
    return TargetType.UNKNOWN_TARGET;
  }
}
