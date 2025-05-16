package com.gpc.geoquake.service;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpc.geoquake.dto.QuakeReportEventDTO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;

class KafkaConsumerServiceImplTest {

  @InjectMocks
  private KafkaConsumerServiceImpl service;

  @Mock
  private KafkaReceiver<String, String> kafkaReceiver;

  @Mock
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testConsumeMessages() throws Exception {
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
    Mockito.when(kafkaReceiver.receive()).thenReturn(Flux.just(mockRecord));

    // Act
    Flux<QuakeReportEventDTO> result = service.consumeMessages();

    // Assert
    List<QuakeReportEventDTO> resultList = result.collectList().block();
    assertNotNull(resultList);
    assertEquals(1, resultList.size());
  }

  @Test
  void testConsumeMessages_whenObjectMapperThrowsException() throws Exception {
    // Arrange
    String mockKey = "key1";
    String mockValue = "{\"field1\":\"value1\",\"field2\":\"value2\"}";

    ReceiverRecord<String, String> mockRecord = Mockito.mock(ReceiverRecord.class);
    ReceiverOffset mockOffset = Mockito.mock(ReceiverOffset.class);

    Mockito.when(mockRecord.key()).thenReturn(mockKey);
    Mockito.when(mockRecord.value()).thenReturn(mockValue);
    Mockito.when(mockRecord.receiverOffset()).thenReturn(mockOffset);
    Mockito.doNothing().when(mockOffset).acknowledge();

    Mockito.when(objectMapper.readValue(mockValue, QuakeReportEventDTO.class)).thenThrow(new RuntimeException("Error de prueba"));
    Mockito.when(kafkaReceiver.receive()).thenReturn(Flux.just(mockRecord));

    // Act & Assert
    assertThrows(RuntimeException.class, () -> {
      service.consumeMessages().blockFirst();
    });
  }
}
