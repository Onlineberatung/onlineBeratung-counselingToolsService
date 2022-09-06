package com.vi.counselingtoolsservice.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vi.counselingtoolsservice.api.authorization.Authority.AuthorityValue;
import com.vi.counselingtoolsservice.api.service.budibase.BudibaseApiService;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.App;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AppsQueryResponse;
import com.vi.counselingtoolsservice.config.BudibaseApiClient;
import com.vi.counselingtoolsservice.config.BudibaseApiClientConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("testing")
@AutoConfigureMockMvc(addFilters = false)
public class AppsControllerTest {

  static final String PATH_GET_AGENCY_BY_ID = "/apps";
  @MockBean
  com.vi.counselingtoolsservice.budibaseApi.generated.web.DefaultApi defaultApi;
  @MockBean
  BudibaseApiClient budibaseApiClient;
  @MockBean
  BudibaseApiClientConfig budibaseApiClientConfig;
  @MockBean
  BudibaseApiService budibaseApiService;
  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();

    App app1 = new App();
    app1.setId(123);
    app1.setBudibaseId("testBudibaseId1");
    app1.setTitle("Test Title 1");
    app1.setDescription("testDescription1");
    app1.setUrl("/testUrl1");
    app1.setEnabled(1);

    App app2 = new App();
    app2.setId(321);
    app2.setBudibaseId("testBudibaseId2");
    app2.setTitle("Test Title 2");
    app2.setDescription("testDescription2");
    app2.setUrl("/testUrl2");
    app2.setEnabled(0);

    AppsQueryResponse appsQueryResponse = new AppsQueryResponse();
    appsQueryResponse.setData(List.of(app1, app2));

    when(budibaseApiService.getApps()).thenReturn(appsQueryResponse);
  }

  @Test
  @WithMockUser(authorities = {AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.USER_DEFAULT})
  public void getApps_Should_Return_ListOfApps() throws Exception {
    mockMvc.perform(get(PATH_GET_AGENCY_BY_ID)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("").isArray());
  }
}