package com.vi.counselingtoolsservice.api.service.budibase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.vi.counselingtoolsservice.budibaseApi.generated.web.DefaultApi;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AssignToolsRequest;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.UserData;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class BudibaseApiServiceTest {

  @InjectMocks
  BudibaseApiService budibaseApiService;

  @Mock
  DefaultApi budbaseClient;

  @Captor
  ArgumentCaptor<String> userId;

  @Captor
  ArgumentCaptor<AssignToolsRequest> request;

  @Before
  public void setup() {
    ReflectionTestUtils.setField(budibaseApiService, // inject into this object
        "budibaseApi", // assign to this field
        budbaseClient);
  }

  @Test
  public void assignTools2OnlineBeratungUser() {
    List<String> appIds = new ArrayList<>();
    appIds.add("app1");
    appIds.add("app2");
    com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User user = new User();
    com.vi.counselingtoolsservice.budibaseApi.generated.web.model.UserData userData = new UserData();
    userData.setId("us_id");
    user.setData(userData);
    Mockito.when(budbaseClient.assignTools(anyString(), any())).thenReturn(user);
    budibaseApiService.assignTools2OnlineBeratungUser("adviceSeekerId", appIds);
    Mockito.verify(budbaseClient).assignTools(userId.capture(), request.capture());
    AssignToolsRequest value = request.getValue();
    MatcherAssert.assertThat(value.getRoles().get("app1"), Matchers.is("BASIC"));
    MatcherAssert.assertThat(value.getRoles().get("app2"), Matchers.is("BASIC"));
  }

}
