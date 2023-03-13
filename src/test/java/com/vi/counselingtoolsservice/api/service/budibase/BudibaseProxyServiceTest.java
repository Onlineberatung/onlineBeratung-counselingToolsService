package com.vi.counselingtoolsservice.api.service.budibase;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAllowedException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class BudibaseProxyServiceTest {

  @InjectMocks
  private BudibaseProxyService budibaseProxyService;

  @Mock
  private BudibaseApiService budibaseApiService;

  @Mock
  private HttpServletRequest request;

  private final String validBudibaseUser = "779a88ce-8679-43e0-8188-5c8c3ffa2d5e";
  private final String validJWTToken = "budibase:auth=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
      + "eyJ1c2VySWQiOiJ1c183NzlhODhjZS04Njc5LTQzZTAtODE4OC01YzhjM2ZmYTJkNWUiLCJzZXNzaW9uSWQiOiJmMW"
      + "NiMTE1N2FjY2I0Mzk0YWRmYjBjNTVmOTUyMzgzNiIsImlhdCI6MTY3NTgxMzc2NH0."
      + "sC_4_UxyumM6_NcILJEpS1ouloNxND2QppKgKow2b4g;";

  @Before
  public void setup() {
    ReflectionTestUtils.setField(budibaseProxyService, // inject into this object
        "budibaseApiService", // assign to this field
        budibaseApiService);
  }

  @Test
  public void consultantRequest_Should_NotThrowException() {
    Mockito.when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.POST;
    Mockito.when(request.getRequestURI()).thenReturn("/api/v2/queries");
    Assertions.assertDoesNotThrow(
        () -> budibaseProxyService
            .validateConsultantRequest(getRequestBody(validBudibaseUser), method, request));
  }

  @Test
  public void consultantRequest_Should_ThrowNotAllowedException_When_BodyParamIsNotInJWT() {
    JSONObject jsonBody = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("bb_user_id", "somecustomid");
    jsonBody.put("parameters", params);
    String body = jsonBody.toString();
    Mockito.when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.POST;
    Mockito.when(request.getRequestURI()).thenReturn("/api/v2/queries");
    Assertions.assertThrows(NotAllowedException.class,
        () -> budibaseProxyService.validateConsultantRequest(body, method, request));
  }

  @Test
  public void consultantRequest_Should_ThrowNotAllowedException_When_Doing_GET() {
    JSONObject jsonBody = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("bb_user_id", "somecustomid");
    jsonBody.put("parameters", params);
    String body = jsonBody.toString();
    Mockito.when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.GET;
    Mockito.when(request.getRequestURI()).thenReturn("/api/v1/queries");
    Assertions.assertThrows(NotAllowedException.class,
        () -> budibaseProxyService.validateConsultantRequest(body, method, request));
  }

  @Test
  public void consultantRequest_Should_NotThrowNotAllowedException_When_QueryOwnData() {
    JSONObject jsonBody = new JSONObject();
    JSONObject query = new JSONObject();
    JSONObject equalJsonObject = new JSONObject();
    equalJsonObject.put("user_id", validBudibaseUser);
    query.put("equal", equalJsonObject);
    jsonBody.put("query", query);
    Mockito.when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.POST;
    Mockito.when(request.getRequestURI()).thenReturn("/api/v1/queries");
    Assertions.assertDoesNotThrow(
        () -> budibaseProxyService
            .validateConsultantRequest(jsonBody.toString(), method, request));
  }

  @Test
  public void consultantRequest_Should_ThrowNotAllowedException_When_QueryBodyNotComplete() {
    JSONObject jsonBody = new JSONObject();
    JSONObject query = new JSONObject();
    JSONObject equalJsonObject = new JSONObject();
    query.put("equal", equalJsonObject);
    jsonBody.put("query", query);
    Mockito.when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.POST;
    Mockito.when(request.getRequestURI()).thenReturn("/api/v1/queries");
    Assertions.assertThrows(BadRequestException.class,
        () -> budibaseProxyService
            .validateConsultantRequest(jsonBody.toString(), method, request));
  }


  @Test
  public void userRequest_Should_ThrowNotAllowedException_When_UserIdInBodyDoesnotMatchesJWT(){
    Mockito.when(request.getHeader("cookie")).thenReturn(validJWTToken);
    Mockito.when(request.getRequestURI()).thenReturn("/some_datasource/rows");
    JSONObject params = new JSONObject();
    params.put("bb_user_id", "customUserId");
    Assertions.assertThrows(NotAllowedException.class,
        () -> budibaseProxyService
            .validateUserRequest(params.toString(), HttpMethod.POST, request));
  }

  @Test
  public void userRequest_Should_NotThrowException_When_UserIdInBodyMatchesJWT(){
    Mockito.when(request.getHeader("cookie")).thenReturn(validJWTToken);
    Mockito.when(request.getRequestURI()).thenReturn("/some_datasource/rows");
    JSONObject params = new JSONObject();
    params.put("bb_user_id", validBudibaseUser);
    Assertions.assertDoesNotThrow(() -> budibaseProxyService
            .validateUserRequest(params.toString(), HttpMethod.POST, request));
  }


  private String getRequestBody(String userId) {
    JSONObject jsonBody = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("bb_user_id", userId);
    jsonBody.put("parameters", params);
    return jsonBody.toString();
  }

}