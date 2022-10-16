package com.vi.counselingtoolsservice.config;

import com.vi.counselingtoolsservice.budibaseApi.generated.ApiClient;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.DefaultApi;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BudibaseApiClientConfig {

  @Value("${budibase.api.url}")
  private String budibaseApiUrl;

  @Value("${budibase.api.key}")
  private String budibaseApiKey;

  @Bean(name="budibaseApiClient")
  @Primary
  public DefaultApi defaultApi(@NonNull RestTemplate restTemplate) {
    ApiClient apiClient = new BudibaseApiClient(restTemplate);
    apiClient.setBasePath(this.budibaseApiUrl);
    apiClient.addDefaultHeader("Accept", "application/json");
    apiClient.addDefaultHeader("Content-Type", "application/json");
    apiClient.addDefaultHeader("x-budibase-api-key", budibaseApiKey);
    return new DefaultApi(apiClient);
  }

}