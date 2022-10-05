package com.vi.counselingtoolsservice.config.httpClient;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

@Configuration
@Slf4j
public class CustomClientErrorHandler implements ResponseErrorHandler {

  @Override
  public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
    return clientHttpResponse.getStatusCode().is4xxClientError();
  }

  @Override
  public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
    log.error("HTTP Status Code: " + clientHttpResponse.getStatusCode().value());
  }
}