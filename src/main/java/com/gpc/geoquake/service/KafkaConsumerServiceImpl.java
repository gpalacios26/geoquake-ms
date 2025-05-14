package com.gpc.geoquake.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpc.geoquake.dto.QuakeReportEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;

@Service
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

  @Value("${spring.kafka.topic.name}")
  private String topicName;

  private final KafkaReceiver<String, String> kafkaReceiver;

  private final ObjectMapper objectMapper;

  public KafkaConsumerServiceImpl(KafkaReceiver<String, String> kafkaReceiver, ObjectMapper objectMapper) {
    this.kafkaReceiver = kafkaReceiver;
    this.objectMapper = objectMapper;
  }

  @Override
  public Flux<QuakeReportEventDTO> consumeMessages() {
    return kafkaReceiver.receive()
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
        });
  }
}
