package com.gpc.geoquake.filter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class RequestFilter implements WebFilter {

  private static final Logger logger = LoggerFactory.getLogger(RequestFilter.class);

  private static final String HEADER_HOST = "X-Request-Host";
  private static final String HEADER_DURATION = "X-Request-Duration";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    long startTime = System.currentTimeMillis();

    String instanceIp = "unknown";
    try {
      instanceIp = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      logger.error("Error al obtener la IP de la instancia.", e);
    }

    String finalInstanceIp = instanceIp;
    exchange.getResponse().beforeCommit(() -> {
      long duration = System.currentTimeMillis() - startTime;
      exchange.getResponse().getHeaders().add(HEADER_HOST, finalInstanceIp);
      exchange.getResponse().getHeaders().add(HEADER_DURATION, duration + "ms");
      return Mono.empty();
    });

    return chain.filter(exchange);
  }
}
