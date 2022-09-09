package com.vi.counselingtoolsservice.api.service.budibase;

import com.vi.counselingtoolsservice.budibaseApi.generated.ApiClient;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.DefaultApi;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AppsQueryResponse;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AssignToolsRequest;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestScope;

@Service
@Slf4j
public class BudibaseApiService {

  @Value("${budibase.appsApp.id}")
  private String budibaseAppsAppId;

  @Value("${budibase.apps.query.id}")
  private String budibaseAppsQueryId;

  @Value("${budibase.api.key}")
  private String budibaseApiKey;

  @Value("${budibase.api.url}")
  private String budibaseApiUrl;

  @Inject
  private DefaultApi budibaseApi;

  public AppsQueryResponse getApps() {
    return budibaseApi.getApps(budibaseAppsQueryId, budibaseAppsAppId);
  }

  public User getBudibaseUser(String adviceSeekerId) {
    return budibaseApi.getUser(convertAdviceSeekerId2BudibaseUserId(adviceSeekerId));
  }

  public User assignTools2OnlineBeratungUser(String adviceSeekerId, List<String> appIds) {
    AssignToolsRequest request = new AssignToolsRequest();
    request.setRoles(new HashMap<>());
    request.setStatus("active");
    appIds.forEach(el -> {
      request.getRoles().put(el, "BASIC");
    });

    User user = budibaseApi
        .assignTools(convertAdviceSeekerId2BudibaseUserId(adviceSeekerId), request);
    user.getData().setId(user.getData().getId().substring(3));
    return user;
  }

  private String convertAdviceSeekerId2BudibaseUserId(String adviceSeekerId) {
    return "us_" + adviceSeekerId;
  }

  @Bean
  @Scope(value = WebApplicationContext.SCOPE_REQUEST)
  public DefaultApi getBudibaseApi() {
    DefaultApi api = new DefaultApi();
    com.vi.counselingtoolsservice.budibaseApi.generated.ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(budibaseApiUrl);
    apiClient.addDefaultHeader("Accept", "application/json");
    apiClient.addDefaultHeader("Content-Type", "application/json");
    apiClient.addDefaultHeader("x-budibase-api-key", budibaseApiKey);
    api.setApiClient(apiClient);
    return api;
  }

  public void assignConsultantTools(String consultantId) {
    List<String> consultantAppIds = getApps().getData().stream()
        .filter(el -> "CONSULTANT_APP".equals(el.getType())).map(el -> el.getBudibaseId())
        .collect(Collectors.toList());
    assignTools2OnlineBeratungUser(consultantId, consultantAppIds);
  }
}
