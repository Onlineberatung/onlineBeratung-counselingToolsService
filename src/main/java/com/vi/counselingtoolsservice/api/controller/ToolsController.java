package com.vi.counselingtoolsservice.api.controller;

import com.vi.counselingtoolsservice.api.facade.ToolsFacade;
import com.vi.counselingtoolsservice.api.model.ApproveUsersAccessToToolRequest;
import com.vi.counselingtoolsservice.api.model.InitialUserToolsImportRequest;
import com.vi.counselingtoolsservice.api.model.Tool;
import com.vi.counselingtoolsservice.api.service.budibase.BudibaseApiService;
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
    return new ResponseEntity<>(toolsFacade.assignAdviceSeekerTools(adviceSeekerId, tools),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> redirectToTool(String consultantId, String toolPath) {
    budibaseApiService.assignConsultantTools(consultantId);
    return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
        .location(toolsFacade.getToolUrl(toolPath)).build();
  }

  @Override
  public ResponseEntity<Void> approveUsersAccessToTool(ApproveUsersAccessToToolRequest request) {
    toolsFacade.approveUsersAccessToTool(request.getNewToolId(), request.getOldToolId());
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @Override
  public ResponseEntity<Void> initialImport(String toolId,
      @Valid InitialUserToolsImportRequest initialUserToolsImportRequest) {
    toolsFacade.initialImport(toolId, initialUserToolsImportRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
