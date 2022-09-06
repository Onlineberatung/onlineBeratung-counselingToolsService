package com.vi.counselingtoolsservice.api.facade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.counselingtoolsservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.counselingtoolsservice.api.model.App;
import com.vi.counselingtoolsservice.api.service.budibase.BudibaseApiService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppFacade {

  @NonNull
  private final BudibaseApiService budibaseApiService;

  /**
   * Unnecessary 1 to 1 conversion needed because App classes comes from separate openapi specs
   *
   * @return list of apps with class of correct package
   */
  public List<App> getApps() {
    java.util.List<com.vi.counselingtoolsservice.api.model.App> apps = new ArrayList<>();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    for (com.vi.counselingtoolsservice.budibaseApi.generated.web.model.App app : Objects.requireNonNull(
        budibaseApiService.getApps().getData())) {
      try {
        apps.add(objectMapper.readValue(new JSONObject(app).toString(), App.class));
      } catch (JsonProcessingException e) {
        throw new InternalServerErrorException("Could not convert budibase apps api response.");
      }
    }
    return apps;
  }

}
