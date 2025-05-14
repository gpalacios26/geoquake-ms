package com.gpc.geoquake.service;

import com.gpc.geoquake.dto.QuakeReportEventDTO;
import reactor.core.publisher.Flux;

public interface KafkaConsumerService {

  Flux<QuakeReportEventDTO> consumeMessages();
}
