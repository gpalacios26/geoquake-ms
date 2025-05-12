package com.gpc.geoquake.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuakeReportDTO {

  private Double maxIntensity;

  private Double minIntensity;

  private List<QuakeReportEventDTO> events;
}
