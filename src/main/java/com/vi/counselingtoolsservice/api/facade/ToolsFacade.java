package com.vi.counselingtoolsservice.api.facade;

import com.vi.counselingtoolsservice.api.model.InitialUserToolsImportRequest;
import com.vi.counselingtoolsservice.api.model.Tool;
import com.vi.counselingtoolsservice.api.model.UserTools;
import com.vi.counselingtoolsservice.api.service.budibase.BudibaseApiService;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.App;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User;
import com.vi.counselingtoolsservice.port.out.UserToolsRepository;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

  @NonNull
  private final UserToolsRepository userToolsRepository;


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

  public List<Tool> assignAdviceSeekerTools(String userId, List<String> toolsIds) {
    budibaseApiService.assignTools2OnlineBeratungUser(userId, toolsIds);
    updateUserTools(userId, toolsIds);
    return getAssignedTools(userId);
  }

  private void updateUserTools(String userId, List<String> toolsIds) {
    Optional<UserTools> userTools = userToolsRepository.findById(userId);
    String newTools = StringUtils.join(toolsIds, ";");
    if (userTools.isPresent()) {
      userTools.get().setTools(newTools);
    } else {
      userTools = Optional.of(new UserTools(userId, newTools));
    }
    userToolsRepository.save(userTools.get());
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

  public void approveUsersAccessToTool(String newToolId, String oldToolId){
    Iterable<UserTools> authorisations = userToolsRepository.findAll();
    authorisations.forEach(userTools -> {
      if (userTools.getTools().contains(oldToolId)) {
        String replace = userTools.getTools().replace(oldToolId, newToolId);
        budibaseApiService.assignTools2OnlineBeratungUser(userTools.getUserId(),
            Arrays.asList(replace.split(";")));
        userTools.setTools(replace);
        userToolsRepository.save(userTools);
      }
    });
  }

  public void initialImport(String toolId,
      @Valid InitialUserToolsImportRequest initialUserToolsImportRequest){
    initialUserToolsImportRequest.getUsers().forEach(toolsImportEntry -> {
      String userId = toolsImportEntry.getKey();
      Optional<UserTools> userTools = userToolsRepository.findById(userId);
      UserTools userToolsEntity;
      if (userTools.isPresent()) {
        userToolsEntity = userTools.get();
        userToolsEntity
            .setTools(userToolsEntity.getTools() + ";" + toolId);
      } else {
        userToolsEntity = new UserTools(userId, toolId);
      }
      userToolsRepository.save(userToolsEntity);
    });


  }
}
