package com.vi.counselingtoolsservice.api.controller;

import com.vi.counselingtoolsservice.api.facade.ToolsFacade;
import com.vi.counselingtoolsservice.api.model.Tool;
import com.vi.counselingtoolsservice.api.service.budibase.BudibaseApiService;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User;
import com.vi.counselingtoolsservice.generated.api.controller.ToolsApi;
import java.util.List;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ToolsController implements ToolsApi {

  @NonNull
  private final ToolsFacade toolsFacade;

  @NonNull
  private final BudibaseApiService budibaseApiService;

  @Override
  public ResponseEntity<List<Tool>> getAdviceSeekerAssignedTools(String adviceSeekerId) {
    return new ResponseEntity<>(toolsFacade.getAssignedTools(adviceSeekerId), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<List<Tool>> assignAdviceSeekerTools(String adviceSeekerId,
      @Valid List<String> tools) {
    User user = budibaseApiService.assignTools2OnlineBeratungUser(adviceSeekerId, tools);
    return new ResponseEntity<>(toolsFacade.getAssignedTools(user.getData().getId()),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> redirectToTool(String consultantId, String toolPath) {
    budibaseApiService.assignConsultantTools(consultantId);
    return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
        .location(toolsFacade.getToolUrl(toolPath)).build();
  }
}
