package com.vi.counselingtoolsservice.api.facade;

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
    Set<String> sharedTools = getSharedTools(adviceSeekerId);
    List<Tool> tools = new ArrayList<>();
    for (App app : Objects
        .requireNonNull(
            budibaseApiService.getApps().getData())) {
      var tool = new Tool();
      tool.setToolId(app.getBudibaseId());
      tool.setTitle(app.getTitle());
      tool.setUrl(budibaseAppBase + "/apps" + tool.getUrl());
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

  public List<Tool> assignTools(String adviceSeekerId, List<String> appIds) {
    User user = budibaseApiService.assignTools(adviceSeekerId, appIds);
    return getAssignedTools(user.getData().getId());
  }

  private Set<String> getSharedTools(String adviceSeekerId) {
    User budibaseUser = budibaseApiService.getBudibaseUser(adviceSeekerId);
    Map<String, String> roles = (Map<String, String>) budibaseUser.getData().getRoles();
    Set<String> sharedTools = roles.keySet();
    return sharedTools;
  }

}
