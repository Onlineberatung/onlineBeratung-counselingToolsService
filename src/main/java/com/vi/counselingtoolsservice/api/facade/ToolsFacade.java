package com.vi.counselingtoolsservice.api.facade;

import com.vi.counselingtoolsservice.api.model.Tool;
import com.vi.counselingtoolsservice.api.service.budibase.BudibaseApiService;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.App;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class ToolsFacade {

  @NonNull
  private final BudibaseApiService budibaseApiService;

  @Value("${budibase.api.url}")
  private String budibaseAppBase;

  public List<Tool> getAssignedTools(String adviceSeekerId) {
    Set<String> sharedTools = getSharedTools(adviceSeekerId);
    List<Tool> tools = new ArrayList<>();

    List<App> apps = budibaseApiService.getApps().getData().stream()
        .filter(el -> "ADVICESEEKER_APP".equals(el.getType()))
        .collect(Collectors.toList());

    for (App app : apps) {
      var tool = new Tool();
      tool.setToolId(app.getBudibaseId());
      tool.setTitle(app.getTitle());
      tool.setUrl(budibaseAppBase + "/app" + app.getUrl());
      tool.setDescription(app.getDescription());
      tool.setSharedWithConsultant(false);
      if (sharedTools.contains(app.getBudibaseId())) {
        tool.setSharedWithAdviceSeeker(true);
      } else {
        tool.setSharedWithAdviceSeeker(false);
      }
      tools.add(tool);
    }
    return tools;
  }

  public List<Object> getInitialQuestionnaireExport() {
    List<HashMap<String, String>> export = new ArrayList<>();
    List<Object> exportData = budibaseApiService.getInitialQuestionnaireExport().getData();

    return exportData;
  }

  private Set<String> getSharedTools(String adviceSeekerId) {
    User budibaseUser = budibaseApiService.getBudibaseUser(adviceSeekerId);

    if (budibaseUser.getData().getId() == null) {
      return new HashSet<>();
    }

    Map<String, String> roles = (Map<String, String>) budibaseUser.getData().getRoles();
    Set<String> sharedTools = roles.keySet();
    return sharedTools;
  }

  public URI getToolUrl(String toolPath) {
    HttpServletRequest curRequest =
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();
    String userId = curRequest.getParameterMap().get("userId")[0];
    return URI.create(budibaseAppBase + "/app/" + toolPath + "?userId=" + userId);
  }
}
