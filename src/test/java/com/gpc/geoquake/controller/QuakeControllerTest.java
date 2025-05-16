package com.gpc.geoquake.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.gpc.geoquake.dto.QuakeDTO;
import com.gpc.geoquake.dto.QuakeReportDTO;
import com.gpc.geoquake.dto.QuakeReportEventDTO;
import com.gpc.geoquake.dto.QuakeReportFilterDTO;
import com.gpc.geoquake.dto.QuakeStreamFilterDTO;
import com.gpc.geoquake.model.Quake;
import com.gpc.geoquake.service.QuakeService;
import java.net.URI;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class QuakeControllerTest {

  @InjectMocks
  private QuakeController controller;

  @Mock
  private QuakeService service;

  @Mock
  private ModelMapper modelMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testFindAll() {
    // Arrange
    Quake quake1 = new Quake();
    Quake quake2 = new Quake();
    QuakeDTO dto1 = new QuakeDTO();
    QuakeDTO dto2 = new QuakeDTO();

    Mockito.when(service.findAll()).thenReturn(Flux.just(quake1, quake2));
    Mockito.when(modelMapper.map(quake1, QuakeDTO.class)).thenReturn(dto1);
    Mockito.when(modelMapper.map(quake2, QuakeDTO.class)).thenReturn(dto2);

    // Act
    Mono<ResponseEntity<Flux<QuakeDTO>>> responseMono = controller.findAll();

    // Assert
    StepVerifier.create(responseMono)
        .assertNext(response -> {
          StepVerifier.create(Objects.requireNonNull(response.getBody()))
              .expectNext(dto1, dto2)
              .verifyComplete();
        })
        .verifyComplete();
  }

  @Test
  void testCreate() {
    // Arrange
    QuakeDTO inputDto = new QuakeDTO();
    Quake model = new Quake();
    QuakeDTO outputDto = new QuakeDTO();
    ServerHttpRequest request = Mockito.mock(ServerHttpRequest.class);

    Mockito.when(modelMapper.map(inputDto, Quake.class)).thenReturn(model);
    Mockito.when(service.save(model)).thenReturn(Mono.just(model));
    Mockito.when(modelMapper.map(model, QuakeDTO.class)).thenReturn(outputDto);
    Mockito.when(request.getURI()).thenReturn(URI.create("http://localhost/api/quake"));

    // Act
    Mono<ResponseEntity<QuakeDTO>> responseMono = controller.create(inputDto, request);

    // Assert
    StepVerifier.create(responseMono)
        .assertNext(response -> {
          assertEquals(outputDto, response.getBody());
          assertTrue(Objects.requireNonNull(response.getHeaders().getLocation()).toString().contains("/api/quake/"));
        })
        .verifyComplete();
  }

  @Test
  void testFindQuakeReport() {
    // Arrange
    QuakeReportFilterDTO filterDTO = new QuakeReportFilterDTO();
    QuakeReportDTO reportDTO = new QuakeReportDTO();

    Mockito.when(service.findQuakeReport(filterDTO)).thenReturn(Mono.just(reportDTO));

    // Act
    Mono<ResponseEntity<QuakeReportDTO>> responseMono = controller.findQuakeReport(filterDTO);

    // Assert
    StepVerifier.create(responseMono)
        .assertNext(response -> {
          assertEquals(reportDTO, response.getBody());
        })
        .verifyComplete();
  }

  @Test
  void testStreamQuakeReport() {
    // Arrange
    QuakeStreamFilterDTO filterDTO = new QuakeStreamFilterDTO();
    QuakeReportEventDTO event1 = new QuakeReportEventDTO();
    QuakeReportEventDTO event2 = new QuakeReportEventDTO();

    Mockito.when(service.streamQuakeReport(filterDTO)).thenReturn(Flux.just(event1, event2));

    // Act
    Flux<QuakeReportEventDTO> result = controller.streamQuakeReport(filterDTO);

    // Assert
    StepVerifier.create(result)
        .expectNext(event1, event2)
        .verifyComplete();
  }
}
