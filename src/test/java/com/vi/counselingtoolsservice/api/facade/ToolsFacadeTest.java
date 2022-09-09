package com.vi.counselingtoolsservice.api.facade;

import com.vi.counselingtoolsservice.api.model.Tool;
import com.vi.counselingtoolsservice.api.service.budibase.BudibaseApiService;
import com.vi.counselingtoolsservice.api.service.budibase.BudibaseApiServiceTest;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.App;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AppsQueryResponse;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.UserData;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RunWith(MockitoJUnitRunner.class)
public class ToolsFacadeTest {

  @InjectMocks
  private ToolsFacade toolsFacade;

  @Mock
  private BudibaseApiService budibaseApiService;

  @Before
  public void setup(){
    ReflectionTestUtils.setField(toolsFacade, // inject into this object
        "budibaseAppBase", // assign to this field
        "www.test.com");
  }

  @Test
  public void getAssignedTools() {
    String adviceSeekerId = "test";
    User budibaseUser = new User();
    UserData budibaseUserData = new UserData();
    budibaseUserData.setId("id");
    Map<String, String> roles = new HashMap<>();
    roles.put("app1", "BASIC");
    budibaseUserData.setRoles(roles);
    budibaseUser.setData(budibaseUserData);
    AppsQueryResponse budibaseApps = new AppsQueryResponse();
    List<App> apps = new ArrayList<>();
    App app1 = new App();
    app1.setType("ADVICESEEKER_APP");
    app1.setBudibaseId("app1");
    app1.setDescription("desc1");
    app1.setTitle("title1");
    app1.setUrl("/url1");
    apps.add(app1);
    App app2 = new App();
    app2.setType("ADVICESEEKER_APP");
    app2.setBudibaseId("app2");
    apps.add(app2);
    App app3 = new App();
    app3.setType("CONSULTANT_APP");
    apps.add(app3);
    budibaseApps.setData(apps);
    Mockito.when(budibaseApiService.getBudibaseUser(adviceSeekerId)).thenReturn(budibaseUser);
    Mockito.when(budibaseApiService.getApps()).thenReturn(budibaseApps);
    List<Tool> assignedTools = toolsFacade.getAssignedTools(adviceSeekerId);

    Tool tool1 = assignedTools.get(0);
    Tool tool2 = assignedTools.get(1);
    MatcherAssert.assertThat(assignedTools.size(), Matchers.is(2));
    MatcherAssert.assertThat(tool1.getDescription(), Matchers.is(app1.getDescription()));
    MatcherAssert.assertThat(tool1.getTitle(), Matchers.is(app1.getTitle()));
    MatcherAssert.assertThat(tool1.getUrl(), Matchers.is("www.test.com/app/url1"));
    MatcherAssert.assertThat(tool1.getSharedWithAdviceSeeker(), Matchers.is(true));

    MatcherAssert.assertThat(tool2.getSharedWithAdviceSeeker(), Matchers.is(false));
  }

  @Test
  public void getToolUrl() {
    var mockRequest = new MockHttpServletRequest();
    mockRequest.setParameter("userId", "userId");
    RequestAttributes att = new ServletRequestAttributes(mockRequest);
    RequestContextHolder.setRequestAttributes(att);
    URI uri = toolsFacade.getToolUrl("toolPath");
    MatcherAssert.assertThat(uri.toString(), Matchers.is("www.test.com/app/toolPath?userId=userId"));
  }

}
