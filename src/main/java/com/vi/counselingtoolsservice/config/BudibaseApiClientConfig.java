package com.vi.counselingtoolsservice.config;

import com.vi.counselingtoolsservice.budibaseApi.generated.ApiClient;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.DefaultApi;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BudibaseApiClientConfig {

  @Value("${budibase.api.url}")
  private String budibaseApiUrl;

  @Value("${budibase.api.key}")
  private String budibaseApiKey;

  @Bean(name = "budibaseApi")
  public DefaultApi defaultApi() {
    final RestTemplate restTemplate = new RestTemplate();
    final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    final HttpClient httpClient = HttpClientBuilder.create()
        .setRedirectStrategy(new LaxRedirectStrategy())
        .build();
    factory.setHttpClient(httpClient);
    restTemplate.setRequestFactory(factory);
    ApiClient apiClient = new BudibaseApiClient(restTemplate);
    apiClient.setBasePath(this.budibaseApiUrl);
    apiClient.setApiKey(this.budibaseApiKey);
    return new DefaultApi(apiClient);
  }

}