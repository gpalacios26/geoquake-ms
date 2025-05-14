package com.gpc.geoquake.service;

import com.gpc.geoquake.dto.QuakeReportEventDTO;
import reactor.core.publisher.Mono;

public interface KafkaProducerService {

  Mono<Void> sendMessage(String key, QuakeReportEventDTO value);
}
