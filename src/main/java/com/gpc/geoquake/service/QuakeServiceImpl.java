package com.gpc.geoquake.service;

import com.gpc.geoquake.dto.GeoDTO;
import com.gpc.geoquake.dto.QuakeReportDTO;
import com.gpc.geoquake.dto.QuakeReportEventDTO;
import com.gpc.geoquake.dto.QuakeReportFilterDTO;
import com.gpc.geoquake.dto.QuakeStreamFilterDTO;
import com.gpc.geoquake.model.Geo;
import com.gpc.geoquake.model.Quake;
import com.gpc.geoquake.repository.QuakeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class QuakeServiceImpl implements QuakeService {

  private final QuakeRepository repository;

  private final GeoService geoService;

  private final KafkaProducerService kafkaProducerService;

  private final KafkaConsumerService kafkaConsumerService;

  private final ModelMapper modelMapper;

  @Override
  public Flux<Quake> findAll() {
    return repository.findAll();
  }

  @Override
  public Mono<Quake> save(Quake quake) {
    return repository.save(quake).doOnNext(saved -> {
        QuakeReportEventDTO quakeReportEventDTO = new QuakeReportEventDTO(saved.getIntensity(), new GeoDTO(saved.getGeo().getLatitude(), saved.getGeo().getLongitude()));
        kafkaProducerService.sendMessage(saved.getId(), quakeReportEventDTO).subscribe();
    });
  }

  @Override
  public Mono<QuakeReportDTO> findQuakeReport(QuakeReportFilterDTO dto) {
    Geo inputGeo = convertGeoToModel(dto.getGeo());
    return findByDate(dto.getDate())
        .collectList()
        .flatMap(quakes -> {
          List<QuakeReportEventDTO> events = getReportEvents(inputGeo, dto.getRadiusKm(), quakes);
          double maxIntensity = events.stream().mapToDouble(QuakeReportEventDTO::getIntensity).max().orElse(0);
          double minIntensity = events.stream().mapToDouble(QuakeReportEventDTO::getIntensity).min().orElse(0);
          QuakeReportDTO quakeReportDTO = new QuakeReportDTO(maxIntensity, minIntensity, events);
          return Mono.just(quakeReportDTO);
        });
  }

  @Override
  public Flux<QuakeReportEventDTO> streamQuakeReport(QuakeStreamFilterDTO dto) {
    Geo inputGeo = convertGeoToModel(dto.getGeo());
    return kafkaConsumerService.consumeMessages()
        .doOnNext(quake -> System.out.printf("Mensaje consumido: %s%n", quake))
        .filter(quake -> {
          List<QuakeReportEventDTO> filteredEvents = getStreamEvents(inputGeo, dto.getRadiusKm(), List.of(quake));
          return !filteredEvents.isEmpty();
        }).map(quake -> {
          System.out.printf("Evento enviado al cliente: %s%n", quake);
          return quake;
        });
  }

  private Flux<Quake> findByDate(LocalDate date) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);
    return repository.findByDateTimeBetween(startOfDay, endOfDay);
  }

  private List<QuakeReportEventDTO> getReportEvents(Geo inputGeo, Double radiusKm, List<Quake> quakes) {
    List<QuakeReportEventDTO> events = new ArrayList<>();
    quakes.forEach(quake -> {
      if (geoService.distanceInKm(inputGeo, quake.getGeo()) <= radiusKm) {
        events.add(new QuakeReportEventDTO(quake.getIntensity(), convertGeoToDto(quake.getGeo())));
      }
    });
    return events;
  }

  private List<QuakeReportEventDTO> getStreamEvents(Geo inputGeo, Double radiusKm, List<QuakeReportEventDTO> quakes) {
    List<QuakeReportEventDTO> events = new ArrayList<>();
    quakes.forEach(quake -> {
      if (geoService.distanceInKm(inputGeo, convertGeoToModel(quake.getGeo())) <= radiusKm) {
        events.add(new QuakeReportEventDTO(quake.getIntensity(), quake.getGeo()));
      }
    });
    return events;
  }

  private GeoDTO convertGeoToDto(Geo model) {
    return modelMapper.map(model, GeoDTO.class);
  }

  private Geo convertGeoToModel(GeoDTO dto) {
    return modelMapper.map(dto, Geo.class);
  }
}
