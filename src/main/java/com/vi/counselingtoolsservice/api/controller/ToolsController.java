package com.vi.counselingtoolsservice.api.controller;

import com.vi.counselingtoolsservice.api.facade.ToolsFacade;
import com.vi.counselingtoolsservice.api.model.Tool;
import com.vi.counselingtoolsservice.generated.api.controller.ToolsApi;
import java.util.List;
import java.util.Map;
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

  @Override
  public ResponseEntity<List<Tool>> getAdviceSeekerAssignedTools(String adviceSeekerId) {
    return new ResponseEntity<>(toolsFacade.getAssignedTools(adviceSeekerId), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<List<Tool>> assignTools(String adviceSeekerId,
      @Valid List<String> tools) {
    return new ResponseEntity<>(toolsFacade.assignTools(adviceSeekerId, tools), HttpStatus.OK);
  }
}
