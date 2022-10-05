package com.vi.counselingtoolsservice.config.httpClient;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@Configuration
@Slf4j
public class CustomClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    // log the http request
    log.info("URI: {}", request.getURI());
    log.info("HTTP Method: {}", request.getMethodValue());
    log.info("HTTP Headers: {}", request.getHeaders());
    return execution.execute(request, body);
  }
}
