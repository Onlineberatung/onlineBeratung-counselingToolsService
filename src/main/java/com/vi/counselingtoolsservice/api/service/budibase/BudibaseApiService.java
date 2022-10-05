package com.vi.counselingtoolsservice.api.service.budibase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.counselingtoolsservice.budibaseApi.generated.ApiClient;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.DefaultApi;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.App;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AppsQueryResponse;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AssignToolsRequest;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.ExportQueryResponse;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User;
import com.vi.counselingtoolsservice.config.BudibaseApiClient;
import java.io.DataInput;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
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

  @Value("${budibase.exportApp.id}")
  private String budibaseExportAppId;

  @Value("${budibase.export.query.id}")
  private String budibaseExportQueryId;

  @Autowired
  @Qualifier("budibaseApiClient")
  private DefaultApi budibaseApi;

  public AppsQueryResponse getApps() {
    return budibaseApi.getApps(budibaseAppsQueryId, budibaseAppsAppId);
  }

  public ExportQueryResponse getInitialQuestionnaireExport() {
    return budibaseApi.getExport(budibaseExportQueryId, budibaseExportAppId);
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

  public void assignConsultantTools(String consultantId) {
    List<String> consultantAppIds = getApps().getData().stream()
        .filter(el -> "CONSULTANT_APP".equals(el.getType())).map(el -> el.getBudibaseId())
        .collect(Collectors.toList());
    assignTools2OnlineBeratungUser(consultantId, consultantAppIds);
  }
}
