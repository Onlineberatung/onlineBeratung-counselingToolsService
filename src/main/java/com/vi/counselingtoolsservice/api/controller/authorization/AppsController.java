package com.vi.counselingtoolsservice.api.controller.authorization;

import com.vi.counselingtoolsservice.api.facade.AppFacade;
import com.vi.counselingtoolsservice.api.model.App;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.vi.counselingtoolsservice.generated.api.controller.AppsApi;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AppsController implements AppsApi {
  @NonNull private final AppFacade appFacade;

  @Override
  public ResponseEntity<List<App>> getApps() {
    return new ResponseEntity<>(appFacade.getApps(), HttpStatus.OK);
  }



}
