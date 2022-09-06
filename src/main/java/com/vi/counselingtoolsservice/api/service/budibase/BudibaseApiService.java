package com.vi.counselingtoolsservice.api.service.budibase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AppsQueryResponse;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.DefaultApi;

@Service
@Slf4j
public class BudibaseApiService {

  @Qualifier("budibaseApi")
  @Autowired
  public void setAdminUserControllerApi(
      DefaultApi budibaseApi) {
    this.budibaseApi = budibaseApi;
  }

  private DefaultApi budibaseApi;

  @Value("${budibase.appsApp.id}")
  private String budibaseAppsAppId;

  public AppsQueryResponse getApps(){
    return budibaseApi.getApps(budibaseAppsAppId);
  }

}
