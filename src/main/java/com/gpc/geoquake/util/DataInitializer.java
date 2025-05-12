package com.gpc.geoquake.util;

import com.gpc.geoquake.model.Geo;
import com.gpc.geoquake.model.Quake;
import com.gpc.geoquake.repository.QuakeRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class DataInitializer {

  private final QuakeRepository repository;

  @PostConstruct
  public void initData() {
    repository
        .deleteAll()
        .thenMany(
            Flux.just(
                    new Quake(
                        null,
                        5.6,
                        35.2,
                        new Geo(-12.046374, -77.042793), // Lima
                        LocalDateTime.of(2025, 5, 12, 10, 20)),
                    new Quake(
                        null,
                        4.8,
                        70.1,
                        new Geo(-16.409047, -71.537451), // Arequipa
                        LocalDateTime.of(2025, 5, 12, 15, 45)),
                    new Quake(
                        null,
                        6.1,
                        120.3,
                        new Geo(-13.163141, -72.545128), // Cusco
                        LocalDateTime.of(2025, 5, 12, 9, 50)),
                    new Quake(
                        null,
                        4.5,
                        15.6,
                        new Geo(-6.486022, -76.365703), // Tarapoto
                        LocalDateTime.of(2025, 5, 13, 19, 30)),
                    new Quake(
                        null,
                        5.9,
                        50.0,
                        new Geo(-3.74912, -73.25383), // Iquitos
                        LocalDateTime.of(2025, 5, 13, 21, 20))
                ).flatMap(repository::save))
        .subscribe(quake -> System.out.println("Inserted: " + quake));
  }
}
