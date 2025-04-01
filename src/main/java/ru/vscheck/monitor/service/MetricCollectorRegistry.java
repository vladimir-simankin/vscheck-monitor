package ru.vscheck.monitor.service;

import ru.vscheck.proto.metrics.MetricType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vscheck.monitor.service.collector.MetricCollector;

@Slf4j
@Service
public class MetricCollectorRegistry {

  private final Map<MetricType, MetricCollector> metricCollectorRepository;

  public MetricCollectorRegistry(List<MetricCollector> checkStrategies) {
    this.metricCollectorRepository =
        checkStrategies.stream()
            .collect(Collectors.toMap(MetricCollector::getSupportedType, Function.identity()));
    log.info(
        "Registered {} metric collectors: [{}]",
        checkStrategies.size(),
        checkStrategies.stream().map(s -> s.getSupportedType().name()).toList());
  }

  public MetricCollector getCheckStrategy(MetricType metricType) {
    return Optional.ofNullable(metricCollectorRepository.get(metricType))
        .orElseThrow(
            () -> new IllegalArgumentException("No metric collector for type: " + metricType));
  }
}
