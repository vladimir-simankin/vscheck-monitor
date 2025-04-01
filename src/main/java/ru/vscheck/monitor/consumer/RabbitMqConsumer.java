package ru.vscheck.monitor.consumer;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.vscheck.monitor.service.MetricCollectorRegistry;
import ru.vscheck.proto.request.MetricRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqConsumer {

  private final MetricCollectorRegistry metricCollectorRegistry;

  @RabbitListener(queues = "${vscheck.rabbit.queues.request}")
  public void receive(Message message) {
    try {
      byte[] messageBody = message.getBody();
      MetricRequest metricRequest = MetricRequest.parseFrom(messageBody);
      log.info("Received metric request: {}", metricRequest);
      metricCollectorRegistry
          .getCheckStrategy(metricRequest.getMetricType())
          .collect(metricRequest);
    } catch (InvalidProtocolBufferException e) {
      log.error("Failed to parse protobuf message", e);
    } catch (Exception e) {
      log.error("Failed to process metric request", e);
    }
  }
}
