package com.gpc.geoquake.service;

import com.gpc.geoquake.dto.QuakeReportDTO;
import com.gpc.geoquake.dto.QuakeReportEventDTO;
import com.gpc.geoquake.dto.QuakeReportFilterDTO;
import com.gpc.geoquake.dto.QuakeStreamFilterDTO;
import com.gpc.geoquake.model.Quake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface QuakeService {

  Flux<Quake> findAll();

  Mono<Quake> save(Quake quake);

  Mono<QuakeReportDTO> findQuakeReport(QuakeReportFilterDTO dto);

  Flux<QuakeReportEventDTO> streamQuakeReport(QuakeStreamFilterDTO dto);
}
