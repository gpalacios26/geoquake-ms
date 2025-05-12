package com.gpc.geoquake.repository;

import com.gpc.geoquake.model.Quake;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface QuakeRepository extends ReactiveMongoRepository<Quake, String> {

  Flux<Quake> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
