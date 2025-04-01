package ru.vscheck.monitor.service.collector;

import ru.vscheck.proto.metrics.Metric;
import ru.vscheck.proto.metrics.MetricType;
import ru.vscheck.proto.request.MetricRequest;

public interface MetricCollector {

  MetricType getSupportedType();

  Metric collect(MetricRequest metricRequest);
}
