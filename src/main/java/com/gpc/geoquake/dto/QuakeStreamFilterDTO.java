package com.gpc.geoquake.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gpc.geoquake.util.CustomDateDeserializer;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuakeStreamFilterDTO {

  @NotNull(message = "El campo geo no puede ser nulo")
  private GeoDTO geo;

  @NotNull(message = "El campo radius km no puede ser nulo")
  private Double radiusKm;
}
