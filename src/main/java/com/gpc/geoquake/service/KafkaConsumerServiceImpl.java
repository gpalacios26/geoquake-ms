package com.gpc.geoquake.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpc.geoquake.dto.QuakeReportEventDTO;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

  private final ReactiveKafkaConsumerTemplate<String, String> kafkaConsumer;

  private final ObjectMapper objectMapper;

  private final Sinks.Many<QuakeReportEventDTO> sink = Sinks.many().multicast().onBackpressureBuffer();

  public KafkaConsumerServiceImpl(ReactiveKafkaConsumerTemplate<String, String> kafkaConsumer, ObjectMapper objectMapper) {
    this.kafkaConsumer = kafkaConsumer;
    this.objectMapper = objectMapper;
    initKafkaMessages();
  }

  public void initKafkaMessages() {
    kafkaConsumer.receive()
        .map(receiverRecord -> {
          try {
            String value = receiverRecord.value();
            QuakeReportEventDTO quakeReportEventDTO = objectMapper.readValue(value, QuakeReportEventDTO.class);

            System.out.printf("Mensaje recibido - Key: %s, Report: %s%n", receiverRecord.key(), quakeReportEventDTO);

            receiverRecord.receiverOffset().acknowledge();
            return quakeReportEventDTO;
          } catch (Exception e) {
            throw new RuntimeException("Error deserializando QuakeReportEventDTO desde JSON", e);
          }
        }).doOnNext(sink::tryEmitNext)
        .subscribe();
  }

  @Override
  public Flux<QuakeReportEventDTO> consumeMessages() {
    return sink.asFlux();
  }
}
