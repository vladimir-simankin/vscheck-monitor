package ru.vscheck.monitor.test;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.vscheck.monitor.service.MetricCollectorRegistry;
import ru.vscheck.proto.metrics.MetricType;

@RestController
@RequiredArgsConstructor
public class TestController {

  private final MetricCollectorRegistry metricCollectorRegistry;

  @PostMapping(value = "test", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public MetricResultJsonDTO getMetric(@RequestBody MetricRequestJsonDTO dto) {
    return MetricResultJsonDTO.fromProto(
        metricCollectorRegistry
            .getCheckStrategy(MetricType.valueOf(dto.getMetricType()))
            .collect(dto.toProto()));
  }
}
