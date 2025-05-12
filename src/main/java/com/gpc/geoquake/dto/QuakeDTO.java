package com.gpc.geoquake.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gpc.geoquake.util.CustomDateTimeDeserializer;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuakeDTO {

  private String id;

  @NotNull(message = "El campo intensity no puede ser nulo")
  private Double intensity;

  @NotNull(message = "El campo deepness no puede ser nulo")
  private Double deepness;

  @NotNull(message = "El campo geo no puede ser nulo")
  private GeoDTO geo;

  @NotNull(message = "El campo date time no puede ser nulo")
  @JsonDeserialize(using = CustomDateTimeDeserializer.class)
  private LocalDateTime dateTime;
}
