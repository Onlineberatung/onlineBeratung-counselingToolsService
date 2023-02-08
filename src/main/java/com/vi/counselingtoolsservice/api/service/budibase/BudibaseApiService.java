package com.vi.counselingtoolsservice.api.service.budibase;

import com.vi.counselingtoolsservice.budibaseApi.generated.ApiClient;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.DefaultApi;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.App;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AssignToolsRequest;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User;
import java.util.ArrayList;
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

  @Value("${budibase.consultantViewApp.id}")
  private String budibaseConsultantViewAppId;

  @Value("${budibase.consultantViewApp.query.id}")
  private String budibaseConsultatViewAppQueryId;

  @Value("${budibase.apps.query.id}")
  private String budibaseAppsQueryId;

  @Value("${budibase.api.key}")
  private String budibaseApiKey;

  @Value("${budibase.api.url}")
  private String budibaseApiUrl;

  @Autowired
  @Qualifier("budibaseClientWithApiKey")
  private DefaultApi budibaseApi;

  public List<App> getApps() {

    LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) budibaseApi
        .executeBudibaseQuery(budibaseAppsQueryId, budibaseAppsAppId, null);

    List<LinkedHashMap<String, Object>> data = (List<LinkedHashMap<String, Object>>) result
        .get("data");
    List<App> apps = new ArrayList<>();
    data.forEach(el -> {
      App app = new App();
      app.setId((Integer) el.get("id"));
      app.setUrl((String) el.get("url"));
      app.setTitle((String) el.get("title"));
      app.setDescription((String) el.get("description"));
      app.setBudibaseId((String) el.get("budibaseId"));
      app.setType((String) el.get("type"));
      apps.add(app);
    });

    return apps;
  }

  public User getBudibaseUser(String adviceSeekerId) {
    return budibaseApi.getUser(convertAdviceSeekerId2BudibaseUserId(adviceSeekerId));
  }

  public List<String> getConsultantAssignedUsers(String consultantId) {
    String body = "{\"parameters\":{\"bb_user_id\":" + "\"" + consultantId + "\"}}";
    LinkedHashMap response;
    response = (LinkedHashMap) budibaseApi
        .executeBudibaseQuery(
            budibaseConsultatViewAppQueryId,
            budibaseConsultantViewAppId, body);
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
    apiClient.addDefaultHeader("Accept-Encoding", "identity");
    api.setApiClient(apiClient);
    return api;
  }

  public void assignConsultantTools(String consultantId) {
    List<String> consultantAppIds = getApps().stream()
        .filter(el -> "CONSULTANT_APP".equals(el.getType())).map(el -> el.getBudibaseId())
        .collect(Collectors.toList());
    assignTools2OnlineBeratungUser(consultantId, consultantAppIds);
  }
}
