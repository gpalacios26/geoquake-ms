package com.gpc.geoquake.service;

import static org.junit.jupiter.api.Assertions.*;

import com.gpc.geoquake.dto.GeoDTO;
import com.gpc.geoquake.dto.QuakeReportEventDTO;
import com.gpc.geoquake.dto.QuakeReportFilterDTO;
import com.gpc.geoquake.dto.QuakeStreamFilterDTO;
import com.gpc.geoquake.model.Geo;
import com.gpc.geoquake.model.Quake;
import com.gpc.geoquake.repository.QuakeRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class QuakeServiceImplTest {

  @InjectMocks
  private QuakeServiceImpl service;

  @Mock
  private QuakeRepository repository;

  @Mock
  private GeoService geoService;

  @Mock
  private KafkaProducerService kafkaProducerService;

  @Mock
  private KafkaConsumerService kafkaConsumerService;

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
    Mockito.when(repository.findAll()).thenReturn(Flux.just(quake1, quake2));

    // Act & Assert
    StepVerifier.create(service.findAll())
        .expectNext(quake1)
        .expectNext(quake2)
        .verifyComplete();

    Mockito.verify(repository, Mockito.times(1)).findAll();
  }

  @Test
  void testSave() {
    // Arrange
    Geo geo = new Geo(10.0, 20.0);
    Quake quake = new Quake();
    quake.setId("12345");
    quake.setGeo(geo);
    quake.setIntensity(5.5);

    Quake savedQuake = new Quake();
    savedQuake.setId("12345");
    savedQuake.setGeo(geo);
    savedQuake.setIntensity(5.5);

    Mockito.when(repository.save(quake)).thenReturn(Mono.just(savedQuake));
    Mockito.when(kafkaProducerService.sendMessage(Mockito.eq("12345"), Mockito.any())).thenReturn(Mono.empty());

    // Act & Assert
    StepVerifier.create(service.save(quake))
        .expectNext(savedQuake)
        .verifyComplete();

    Mockito.verify(repository, Mockito.times(1)).save(quake);
    Mockito.verify(kafkaProducerService, Mockito.times(1)).sendMessage(Mockito.eq("12345"), Mockito.any());
  }

  @Test
  void testFindQuakeReport() {
    // Arrange
    GeoDTO geoDTO = new GeoDTO(10.0, 20.0);
    LocalDate date = LocalDate.now();
    QuakeReportFilterDTO filterDTO = new QuakeReportFilterDTO();
    filterDTO.setGeo(geoDTO);
    filterDTO.setDate(date);
    filterDTO.setRadiusKm(100.0);

    Geo inputGeo = new Geo(10.0, 20.0);
    Quake quake = new Quake();
    quake.setGeo(inputGeo);
    quake.setIntensity(6.0);

    Mockito.when(modelMapper.map(geoDTO, Geo.class)).thenReturn(inputGeo);
    Mockito.when(repository.findByDateTimeBetween(Mockito.any(), Mockito.any())).thenReturn(Flux.just(quake));
    Mockito.when(geoService.distanceInKm(Mockito.any(), Mockito.any())).thenReturn(50.0);
    Mockito.when(modelMapper.map(Mockito.any(Geo.class), Mockito.eq(GeoDTO.class))).thenReturn(geoDTO);

    // Act & Assert
    StepVerifier.create(service.findQuakeReport(filterDTO))
        .assertNext(report -> {
          assertEquals(6.0, report.getMaxIntensity());
          assertEquals(6.0, report.getMinIntensity());
          assertFalse(report.getEvents().isEmpty());
        })
        .verifyComplete();
  }

  @Test
  void testFindQuakeReport_whenNoQuakesFound() {
    // Arrange
    GeoDTO geoDTO = new GeoDTO(10.0, 20.0);
    LocalDate date = LocalDate.now();
    QuakeReportFilterDTO filterDTO = new QuakeReportFilterDTO();
    filterDTO.setGeo(geoDTO);
    filterDTO.setDate(date);
    filterDTO.setRadiusKm(100.0);

    Geo inputGeo = new Geo(10.0, 20.0);

    Mockito.when(modelMapper.map(geoDTO, Geo.class)).thenReturn(inputGeo);
    Mockito.when(repository.findByDateTimeBetween(Mockito.any(), Mockito.any())).thenReturn(Flux.empty());

    // Act & Assert
    StepVerifier.create(service.findQuakeReport(filterDTO))
        .assertNext(report -> {
          assertEquals(0.0, report.getMaxIntensity());
          assertEquals(0.0, report.getMinIntensity());
          assertTrue(report.getEvents().isEmpty());
        })
        .verifyComplete();
  }

  @Test
  void testFindQuakeReport_whenDistanceGreaterThanRadius() {
    // Arrange
    GeoDTO geoDTO = new GeoDTO(10.0, 20.0);
    LocalDate date = LocalDate.now();
    QuakeReportFilterDTO filterDTO = new QuakeReportFilterDTO();
    filterDTO.setGeo(geoDTO);
    filterDTO.setDate(date);
    filterDTO.setRadiusKm(10.0);

    Geo inputGeo = new Geo(10.0, 20.0);
    Quake quake = new Quake();
    quake.setGeo(inputGeo);
    quake.setIntensity(6.0);

    Mockito.when(modelMapper.map(geoDTO, Geo.class)).thenReturn(inputGeo);
    Mockito.when(repository.findByDateTimeBetween(Mockito.any(), Mockito.any())).thenReturn(Flux.just(quake));
    Mockito.when(geoService.distanceInKm(Mockito.any(), Mockito.any())).thenReturn(50.0);
    Mockito.when(modelMapper.map(Mockito.any(Geo.class), Mockito.eq(GeoDTO.class))).thenReturn(geoDTO);

    // Act & Assert
    StepVerifier.create(service.findQuakeReport(filterDTO))
        .assertNext(report -> {
          assertEquals(0.0, report.getMaxIntensity());
          assertEquals(0.0, report.getMinIntensity());
          assertTrue(report.getEvents().isEmpty());
        })
        .verifyComplete();
  }

  @Test
  void testStreamQuakeReport() {
    // Arrange
    Geo inputGeo = new Geo(10.0, 20.0);
    GeoDTO geoDTO = new GeoDTO(10.0, 20.0);
    GeoDTO geoDTO2 = new GeoDTO(10.1, 20.1);
    QuakeStreamFilterDTO filterDTO = new QuakeStreamFilterDTO();
    filterDTO.setGeo(geoDTO);
    filterDTO.setRadiusKm(100.0);

    QuakeReportEventDTO event = new QuakeReportEventDTO(6.0, geoDTO);
    QuakeReportEventDTO event2 = new QuakeReportEventDTO(6.0, geoDTO2);

    Mockito.when(modelMapper.map(geoDTO, Geo.class)).thenReturn(inputGeo);
    Mockito.when(geoService.distanceInKm(Mockito.eq(inputGeo), Mockito.any())).thenReturn(50.0);
    Mockito.when(kafkaConsumerService.consumeMessages()).thenReturn(Flux.just(event, event2));

    // Act & Assert
    StepVerifier.create(service.streamQuakeReport(filterDTO))
        .expectNext(event)
        .expectNext(event2)
        .verifyComplete();
  }

  @Test
  void testStreamQuakeReport_whenDistanceGreaterThanRadius() {
    // Arrange
    Geo inputGeo = new Geo(10.0, 20.0);
    GeoDTO geoDTO = new GeoDTO(10.0, 20.0);
    GeoDTO geoDTO2 = new GeoDTO(10.1, 20.1);
    QuakeStreamFilterDTO filterDTO = new QuakeStreamFilterDTO();
    filterDTO.setGeo(geoDTO);
    filterDTO.setRadiusKm(100.0);

    QuakeReportEventDTO event = new QuakeReportEventDTO(6.0, geoDTO);
    QuakeReportEventDTO event2 = new QuakeReportEventDTO(6.0, geoDTO2);

    Mockito.when(modelMapper.map(geoDTO, Geo.class)).thenReturn(inputGeo);
    Mockito.when(geoService.distanceInKm(Mockito.eq(inputGeo), Mockito.any())).thenReturn(150.0);
    Mockito.when(kafkaConsumerService.consumeMessages()).thenReturn(Flux.just(event, event2));

    // Act & Assert
    StepVerifier.create(service.streamQuakeReport(filterDTO))
        .verifyComplete();
  }
}
