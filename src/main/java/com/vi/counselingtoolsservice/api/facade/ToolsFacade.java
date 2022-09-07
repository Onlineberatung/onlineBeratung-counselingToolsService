package com.vi.counselingtoolsservice.api.facade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.counselingtoolsservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.counselingtoolsservice.api.model.Tool;
import com.vi.counselingtoolsservice.api.service.budibase.BudibaseApiService;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.App;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ToolsFacade {

  @NonNull
  private final BudibaseApiService budibaseApiService;

  @Value("${budibase.api.url}")
  private String budibaseAppBase;

  public List<Tool> getAssignedTools(String adviceSeekerId) {

    //TODO: check is advice seeker assigned to consultant

    Set<String> sharedTools = getSharedTools(adviceSeekerId);
    ObjectMapper objectMapper = getObjectMapper();
    List<Tool> tools = new ArrayList<>();
    for (App app : Objects
        .requireNonNull(
            budibaseApiService.getApps().getData())) {
      try {
        var tool = objectMapper.readValue(new JSONObject(app).toString(), Tool.class);
        if (sharedTools.contains(app.getBudibaseId())) {
          tool.setSharedWithAdviceSeeker(true);
        } else {
          tool.setSharedWithAdviceSeeker(false);
        }
        tool.setSharedWithConsultant(false);
        tool.setUrl(budibaseAppBase + "/apps" + tool.getUrl());
        tools.add(tool);
      } catch (JsonProcessingException e) {
        throw new InternalServerErrorException("Could not convert budibase apps api response.");
      }
    }
    return tools;
  }

  public List<Tool> assignTools(String adviceSeekerId, List<String> appIds) {
    User user = budibaseApiService.assignTools(adviceSeekerId, appIds);
    return getAssignedTools(user.getData().getId());
  }

  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return objectMapper;
  }

  private Set<String> getSharedTools(String adviceSeekerId) {
    User budibaseUser = budibaseApiService.getBudibaseUser(adviceSeekerId);
    Map<String, String> roles = (Map<String, String>) budibaseUser.getData().getRoles();
    Set<String> sharedTools = roles.keySet();
    return sharedTools;
  }

}
