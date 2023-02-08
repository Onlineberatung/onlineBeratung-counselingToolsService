package com.vi.counselingtoolsservice.api.service.budibase;

import net.minidev.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BudibaseProxyAuthServiceTest {

  private BusibaseProxyAuthService busibaseProxyAuthService = new BusibaseProxyAuthService();

  @Test
  public void identifyNonAdminUser_Should_ReturnConsultant(){
    JSONObject responseObject = new JSONObject();
    JSONObject thirdPartyProfileObject = new JSONObject();
    thirdPartyProfileObject.put("groups", new JSONArray().appendElement("consultant"));
    responseObject.put("thirdPartyProfile", thirdPartyProfileObject);
    String role = busibaseProxyAuthService.identifyNonAdminUser(responseObject.toString());
    Assertions.assertEquals(role,"consultant");
  }

  @Test
  public void identifyNonAdminUser_Should_ReturnUser(){
    JSONObject responseObject = new JSONObject();
    JSONObject thirdPartyProfileObject = new JSONObject();
    thirdPartyProfileObject.put("groups", new JSONArray().appendElement("user"));
    responseObject.put("thirdPartyProfile", thirdPartyProfileObject);
    String role = busibaseProxyAuthService.identifyNonAdminUser(responseObject.toString());
    Assertions.assertEquals(role,"user");
  }

}
