package com.gpc.geoquake.service;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpc.geoquake.dto.QuakeReportEventDTO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;

class KafkaConsumerServiceImplTest {

  @Mock
  private ReactiveKafkaConsumerTemplate<String, String> kafkaConsumer;

  @Mock
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testInitKafkaMessages() throws Exception {
    // Arrange
    String mockKey = "key1";
    String mockValue = "{\"field1\":\"value1\",\"field2\":\"value2\"}";
    QuakeReportEventDTO mockDto = new QuakeReportEventDTO();

    ReceiverRecord<String, String> mockRecord = Mockito.mock(ReceiverRecord.class);
    ReceiverOffset mockOffset = Mockito.mock(ReceiverOffset.class);

    Mockito.when(mockRecord.key()).thenReturn(mockKey);
    Mockito.when(mockRecord.value()).thenReturn(mockValue);
    Mockito.when(mockRecord.receiverOffset()).thenReturn(mockOffset);
    Mockito.doNothing().when(mockOffset).acknowledge();

    Mockito.when(objectMapper.readValue(mockValue, QuakeReportEventDTO.class)).thenReturn(mockDto);
    Mockito.when(kafkaConsumer.receive()).thenReturn(Flux.just(mockRecord));

    // Act
    KafkaConsumerServiceImpl testService = new KafkaConsumerServiceImpl(kafkaConsumer, objectMapper);
    testService.initKafkaMessages();

    // Assert
    Mockito.verify(kafkaConsumer, Mockito.atLeastOnce()).receive();
  }

  @Test
  void testConsumeMessages() throws Exception {
    // Arrange
    QuakeReportEventDTO dto = new QuakeReportEventDTO();
    ReceiverRecord<String, String> mockRecord = Mockito.mock(ReceiverRecord.class);
    ReceiverOffset mockOffset = Mockito.mock(ReceiverOffset.class);

    Mockito.when(mockRecord.key()).thenReturn("key1");
    Mockito.when(mockRecord.value()).thenReturn("{\"field1\":\"value1\",\"field2\":\"value2\"}");
    Mockito.when(mockRecord.receiverOffset()).thenReturn(mockOffset);
    Mockito.doNothing().when(mockOffset).acknowledge();
    Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.eq(QuakeReportEventDTO.class))).thenReturn(dto);
    Mockito.when(kafkaConsumer.receive()).thenReturn(Flux.just(mockRecord));

    // Act
    KafkaConsumerServiceImpl testService = new KafkaConsumerServiceImpl(kafkaConsumer, objectMapper);
    List<QuakeReportEventDTO> result = testService.consumeMessages().take(1).collectList().block();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(dto, result.get(0));
  }
}
