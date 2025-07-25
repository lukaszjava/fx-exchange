package com.example.fx.infrastructure.config;

import io.github.bucket4j.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  @Value("${rate‑limit.capacity:50}")
  private long capacity;

  @Value("${rate‑limit.refill:50}")
  private long refillTokens;

  @Value("${rate‑limit.window:PT1M}")
  private Duration window;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    String key = resolveClientKey(request);
    Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) {
      response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
      filterChain.doFilter(request, response);
    } else {
      long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setHeader("Retry-After", String.valueOf(waitSeconds));
      log.warn("Rate‑limit exceeded for key {} – wait {} s", key, waitSeconds);
    }
  }

  private Bucket newBucket() {
    Bandwidth limit =
        Bandwidth.builder().capacity(capacity).refillGreedy(refillTokens, window).build();
    return Bucket.builder().addLimit(limit).build();
  }

  private static String resolveClientKey(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    return (forwarded != null && !forwarded.isBlank())
        ? forwarded.split(",")[0].trim()
        : request.getRemoteAddr();
  }
}
