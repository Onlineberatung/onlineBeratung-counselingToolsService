package com.vi.counselingtoolsservice.config.httpClient;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HeaderIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Slf4j
public class ApacheHttpClientConfig {

  final Integer DEFAULT_KEEP_ALIVE_TIME = 60000;
  final Integer CONNECT_TIMEOUT = 30000;
  final Integer REQUEST_TIMEOUT = 5000;
  final Integer SOCKET_TIMEOUT = 60000;

  @Bean
  public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
    return (httpResponse, httpContext) -> {
      HeaderIterator headerIterator = httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE);
      HeaderElementIterator elementIterator = new BasicHeaderElementIterator(headerIterator);

      while (elementIterator.hasNext()) {
        HeaderElement element = elementIterator.nextElement();
        String param = element.getName();
        String value = element.getValue();
        if (value != null && param.equalsIgnoreCase("timeout")) {
          return Long.parseLong(value) * 1000; // convert to ms
        }
      }

      return DEFAULT_KEEP_ALIVE_TIME;
    };
  }

  @Bean
  public CloseableHttpClient httpClient() {
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(CONNECT_TIMEOUT)
        .setConnectionRequestTimeout(REQUEST_TIMEOUT)
        .setSocketTimeout(SOCKET_TIMEOUT)
        .build();
    return HttpClients.custom()
        .setDefaultRequestConfig(requestConfig)
        .setKeepAliveStrategy(connectionKeepAliveStrategy())
        .build();
  }


}