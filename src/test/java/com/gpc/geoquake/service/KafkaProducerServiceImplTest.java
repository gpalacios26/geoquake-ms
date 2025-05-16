package com.gpc.geoquake.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpc.geoquake.dto.QuakeReportEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;

class KafkaProducerServiceImplTest {

  @InjectMocks
  private KafkaProducerServiceImpl service;

  @Mock
  private KafkaSender<String, String> kafkaSender;

  @Mock
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(service, "topicName", "mock-topic");
  }

  @Test
  void testSendMessage() throws Exception {
    // Arrange
    String key = "testKey";
    QuakeReportEventDTO value = new QuakeReportEventDTO();
    String valueAsString = "{\"example\":\"data\"}";

    Mockito.when(objectMapper.writeValueAsString(value)).thenReturn(valueAsString);
    Mockito.when(kafkaSender.send(any())).thenReturn(Flux.empty());

    // Act
    Mono<Void> result = service.sendMessage(key, value);

    // Assert
    assertDoesNotThrow(() -> result.block());
    Mockito.verify(objectMapper).writeValueAsString(value);
    Mockito.verify(kafkaSender).send(any());
  }

  @Test
  void testSendMessage_whenObjectMapperThrowsException() throws Exception {
    // Arrange
    String key = "testKey";
    QuakeReportEventDTO value = new QuakeReportEventDTO();

    Mockito.when(objectMapper.writeValueAsString(value)).thenThrow(JsonProcessingException.class);

    // Act & Assert
    assertThrows(RuntimeException.class, () -> {
      service.sendMessage(key, value).block();
    });
  }
}
