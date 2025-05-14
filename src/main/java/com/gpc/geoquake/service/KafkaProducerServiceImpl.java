package com.gpc.geoquake.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpc.geoquake.dto.QuakeReportEventDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

  @Value("${spring.kafka.topic.name}")
  private String topicName;

  private final KafkaSender<String, String> kafkaSender;

  private final ObjectMapper objectMapper;

  public KafkaProducerServiceImpl(KafkaSender<String, String> kafkaSender, ObjectMapper objectMapper) {
    this.kafkaSender = kafkaSender;
    this.objectMapper = objectMapper;
  }

  @Override
  public Mono<Void> sendMessage(String key, QuakeReportEventDTO value) {
    try {
      String valueAsString = objectMapper.writeValueAsString(value);

      System.out.printf("Mensaje enviado - Key: %s, Report: %s%n", key, valueAsString);

      return kafkaSender.send(Mono.just(SenderRecord.create(
          new ProducerRecord<>(topicName, key, valueAsString), null
      ))).then();
    } catch (JsonProcessingException e) {
      return Mono.error(new RuntimeException("Error serializando QuakeReportEventDTO a JSON", e));
    }
  }
}
