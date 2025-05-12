package com.gpc.geoquake.service;

import com.gpc.geoquake.dto.GeoDTO;
import com.gpc.geoquake.dto.QuakeReportDTO;
import com.gpc.geoquake.dto.QuakeReportEventDTO;
import com.gpc.geoquake.dto.QuakeReportFilterDTO;
import com.gpc.geoquake.model.Geo;
import com.gpc.geoquake.model.Quake;
import com.gpc.geoquake.repository.QuakeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class QuakeServiceImpl implements QuakeService {

  private final QuakeRepository repository;

  private final GeoService geoService;

  @Override
  public Flux<Quake> findAll() {
    return repository.findAll();
  }

  @Override
  public Mono<Quake> save(Quake quake) {
    return repository.save(quake);
  }

  @Override
  public Mono<QuakeReportDTO> findQuakeReport(QuakeReportFilterDTO dto) {
    Geo inputGeo = new Geo(dto.getGeo().getLatitude(), dto.getGeo().getLongitude());
    return findByDate(dto.getDate())
        .collectList()
        .flatMap(quakes -> {
          List<QuakeReportEventDTO> events = new ArrayList<>();
          quakes.forEach(quake -> {
            if (geoService.distanceInKm(inputGeo, quake.getGeo()) <= dto.getRadiusKm()) {
              events.add(new QuakeReportEventDTO(quake.getIntensity(), new GeoDTO(quake.getGeo().getLatitude(), quake.getGeo().getLongitude())));
            }
          });
          double maxIntensity = events.stream().mapToDouble(QuakeReportEventDTO::getIntensity).max().orElse(0);
          double minIntensity = events.stream().mapToDouble(QuakeReportEventDTO::getIntensity).min().orElse(0);
          QuakeReportDTO quakeReportDTO = new QuakeReportDTO(maxIntensity, minIntensity, events);
          return Mono.just(quakeReportDTO);
        });
  }

  private Flux<Quake> findByDate(LocalDate date) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);
    return repository.findByDateTimeBetween(startOfDay, endOfDay);
  }
}
