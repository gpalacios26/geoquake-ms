package com.gpc.geoquake.controller;

import com.gpc.geoquake.dto.QuakeDTO;
import com.gpc.geoquake.dto.QuakeReportDTO;
import com.gpc.geoquake.dto.QuakeReportFilterDTO;
import com.gpc.geoquake.model.Quake;
import com.gpc.geoquake.service.QuakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/quake")
@RequiredArgsConstructor
@Tag(name = "Quake API", description = "Documentación de la API de Movimientos Telúricos")
public class QuakeController {

  private final QuakeService service;

  private final ModelMapper modelMapper;

  @GetMapping
  @Operation(summary = "Listar Movimientos Telúricos", description = "Devuelve todos los registros de movimientos telúricos")
  public Mono<ResponseEntity<Flux<QuakeDTO>>> findAll() {
    Flux<QuakeDTO> fx = service.findAll().map(this::convertToDto);
    return Mono.just(ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(fx)
    ).defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping
  @Operation(summary = "Crear Movimiento Telúrico", description = "Registro de un movimiento telúrico")
  public Mono<ResponseEntity<QuakeDTO>> create(@Valid @RequestBody QuakeDTO dto, final ServerHttpRequest req) {
    return service.save(this.convertToModel(dto))
        .map(model -> ResponseEntity
            .created(URI.create(req.getURI().toString().concat("/").concat(String.valueOf(model.getId()))))
            .contentType(MediaType.APPLICATION_JSON)
            .body(this.convertToDto(model))
        ).defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping("/report")
  @Operation(summary = "Movimiento Telúrico - Reporte", description = "Devuelve un reporte de movimientos telúricos")
  public Mono<ResponseEntity<QuakeReportDTO>> findQuakeReport(@Valid @RequestBody QuakeReportFilterDTO dto) {
    return service.findQuakeReport(dto)
        .map(exchange -> ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(exchange)
        )
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  private QuakeDTO convertToDto(Quake model) {
    return modelMapper.map(model, QuakeDTO.class);
  }

  private Quake convertToModel(QuakeDTO dto) {
    return modelMapper.map(dto, Quake.class);
  }
}
