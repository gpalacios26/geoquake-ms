package com.gpc.geoquake.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "quake")
public class Quake {

  @Id
  private String id;

  @Field
  private Double intensity;

  @Field
  private Double deepness;

  @Field
  private Geo geo;

  @Field(name = "date_time")
  private LocalDateTime dateTime;
}
