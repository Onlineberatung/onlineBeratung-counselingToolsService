package com.vi.counselingtoolsservice.api.service.budibase;

import com.vi.counselingtoolsservice.budibaseApi.generated.ApiClient;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.DefaultApi;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AppsQueryResponse;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AssignToolsRequest;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

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

  @Autowired
  @Qualifier("budibaseClientWithApiKey")
  private DefaultApi budibaseApi;

  public AppsQueryResponse getApps() {
    return (AppsQueryResponse) budibaseApi
        .executeBudibaseQuery(budibaseAppsQueryId, budibaseAppsAppId, null);
  }

  public User getBudibaseUser(String adviceSeekerId) {
    return budibaseApi.getUser(convertAdviceSeekerId2BudibaseUserId(adviceSeekerId));
  }

  public List<String> getConsultantAssignedUsers(String consultantId) {
    String body = "{\"parameters\":{\"bb_user_id\":"+ "\""+ consultantId+ "\"}}";
    LinkedHashMap response;
    response = (LinkedHashMap) budibaseApi
        .executeBudibaseQuery("query_datasource_plus_dc92c76ee8214e649ff5d91f8c85dfca_a479a18c2df34b93bd8a52fba4b1f7a2", "app_bb7ec2c61a1e4ee7b4f217a852a976eb", body);
    List data = (List) response.get("data");
    return (List<String>) data.stream().map(el -> ((LinkedHashMap) el).get("user_id"))
        .collect(Collectors.toList());
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

  @Bean(name = "budibaseClientWithApiKey")
  public DefaultApi buildBudibaseApiClient() {
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
