package com.gpc.geoquake.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoDTO {

  @NotNull(message = "El campo latitude no puede ser nulo")
  private Double latitude;

  @NotNull(message = "El campo longitude no puede ser nulo")
  private Double longitude;
}
