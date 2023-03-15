package com.vi.counselingtoolsservice.api.service.budibase;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAllowedException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BudibaseProxyServiceTest {

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

  @BeforeEach
  public void setup() {
    ReflectionTestUtils.setField(budibaseProxyService, // inject into this object
        "budibaseApiService", // assign to this field
        budibaseApiService);
  }

  @Test
  void consultantRequest_Should_NotThrowException() {
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.POST;
    when(request.getRequestURI()).thenReturn("/api/v2/queries");
    Assertions.assertDoesNotThrow(
        () -> budibaseProxyService
            .validateConsultantRequest(getRequestBody(validBudibaseUser), method, request));
  }

  @Test
  void consultantRequest_Should_ThrowNotAllowedException_When_BodyParamIsNotInJWT() {
    JSONObject jsonBody = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("bb_user_id", "somecustomid");
    jsonBody.put("parameters", params);
    String body = jsonBody.toString();
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.POST;
    when(request.getRequestURI()).thenReturn("/api/v2/queries");
    Assertions.assertThrows(NotAllowedException.class,
        () -> budibaseProxyService.validateConsultantRequest(body, method, request));
  }

  @Test
  void consultantRequest_Should_ThrowNotAllowedException_When_Doing_GET() {
    JSONObject jsonBody = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("bb_user_id", "somecustomid");
    jsonBody.put("parameters", params);
    String body = jsonBody.toString();
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.GET;
    when(request.getRequestURI()).thenReturn("/api/v1/queries");
    Assertions.assertThrows(NotAllowedException.class,
        () -> budibaseProxyService.validateConsultantRequest(body, method, request));
  }


  @ParameterizedTest
  @ValueSource(strings = {"/api/global/self", "/api/self", "/api/routing/client"})
  void consultantRequest_Should_AllowCallForApiSelfAndRoutingEndpoints_When_UserIdMatchingInTheCookie(String path) {
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.GET;
    when(request.getParameter("bb_user_id")).thenReturn(validBudibaseUser);
    when(request.getRequestURI()).thenReturn(path + "?bb_user_id="+ validBudibaseUser);
    try {
      validateConsultantRequestWithEmptyBody(method);
    } catch (Exception e) {
      fail("no exception should be thrown");
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/global/self", "/api/self", "/api/routing/client"})
  void consultantRequest_Should_AllowCallForApiSelfAndRoutingEndpoints_When_NoUserIdProvidedInRequest(String path) {
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.GET;
    when(request.getRequestURI()).thenReturn(path);
    try {
      validateConsultantRequestWithEmptyBody(method);
    } catch (Exception e) {
      fail("no exception should be thrown");
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/global/self", "/api/self", "/api/routing/client"})
  void consultantRequest_Should_ThrowExceptionIfAttemptToGetSelfDataOrRoutingDataOnBehalfOfOtherUser(String path) {
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.GET;
    when(request.getParameter("bb_user_id")).thenReturn("other user");
    when(request.getRequestURI()).thenReturn(path + "bb_user_id=" + validBudibaseUser);
    Assertions.assertThrows(NotAllowedException.class,
        () -> validateConsultantRequestWithEmptyBody(method));
  }


  @Test
  void consultantRequest_Should_NotThrowNotAllowedException_When_QueryOwnData() {
    JSONObject jsonBody = new JSONObject();
    JSONObject query = new JSONObject();
    JSONObject equalJsonObject = new JSONObject();
    equalJsonObject.put("user_id", validBudibaseUser);
    query.put("equal", equalJsonObject);
    jsonBody.put("query", query);
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.POST;
    when(request.getRequestURI()).thenReturn("/api/v1/queries");
    Assertions.assertDoesNotThrow(
        () -> validateConsultantRequestWithNonEmptyBody(jsonBody, method));
  }

  @Test
  void consultantRequest_Should_ThrowNotAllowedException_When_QueryBodyNotComplete() {
    JSONObject jsonBody = new JSONObject();
    JSONObject query = new JSONObject();
    JSONObject equalJsonObject = new JSONObject();
    query.put("equal", equalJsonObject);
    jsonBody.put("query", query);
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.POST;
    when(request.getRequestURI()).thenReturn("/api/v1/queries");
    Assertions.assertThrows(BadRequestException.class,
        () -> validateConsultantRequestWithNonEmptyBody(jsonBody, method));
  }

  @Test
  void userRequest_Should_ThrowNotAllowedException_When_UserIdInBodyDoesnotMatchesJWT(){
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    when(request.getRequestURI()).thenReturn("/some_datasource/rows");
    JSONObject params = new JSONObject();
    params.put("bb_user_id", "customUserId");
    Assertions.assertThrows(NotAllowedException.class,
        () -> executeValidateUserRequestWithBody(params));
  }

  @Test
  void userRequest_Should_NotThrowException_When_UserIdInBodyMatchesJWT(){
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    when(request.getRequestURI()).thenReturn("/some_datasource/rows");
    JSONObject params = new JSONObject();
    params.put("bb_user_id", validBudibaseUser);
    Assertions.assertDoesNotThrow(() -> executeValidateUserRequestWithBody(params));
  }


  @ParameterizedTest
  @ValueSource(strings = {"/api/global/self", "/api/self", "/api/routing/client"})
  void userRequest_Should_AllowCallToApiSelfOrRouting_When_UserIdMatchingInTheCookie(String path) {
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.GET;
    when(request.getParameter("bb_user_id")).thenReturn(validBudibaseUser);
    when(request.getRequestURI()).thenReturn(path + "?bb_user_id=" + validBudibaseUser);
    try {
      executeValidateUserRequest(method);
    } catch (Exception e) {
      fail("no exception should be thrown");
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/global/self", "/api/self", "/api/routing/client"})
  void userRequest_Should_AllowCallToApiSelfOrRouting_When_ThereAreNoRequestParams(String path) {
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.GET;
    when(request.getRequestURI()).thenReturn(path + "?bb_user_id="+ validBudibaseUser);
    try {
      executeValidateUserRequest(method);
    } catch (Exception e) {
      fail("no exception should be thrown");
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/global/self", "/api/self", "/api/routing/client"})
  void userRequest_Should_ThrowException_When_AttemptToGetSelfOrRoutingDataOnBehalfOfOtherUser(String path) {
    when(request.getHeader("cookie")).thenReturn(validJWTToken);
    HttpMethod method = HttpMethod.GET;
    when(request.getParameter("bb_user_id")).thenReturn("other user");
    when(request.getRequestURI()).thenReturn(path + "?bb_user_id=" + validBudibaseUser);
    Assertions.assertThrows(NotAllowedException.class,
        () -> executeValidateUserRequest(method));
  }

  private void validateConsultantRequestWithEmptyBody(HttpMethod method) {
    budibaseProxyService.validateConsultantRequest(getEmptyBody(), method, request);
  }

  private static String getEmptyBody() {
    return new JSONObject().toString();
  }

  private void executeValidateUserRequestWithBody(JSONObject params) {
    budibaseProxyService
        .validateUserRequest(params.toString(), HttpMethod.POST, request);
  }

  private void validateConsultantRequestWithNonEmptyBody(JSONObject jsonBody, HttpMethod method) {
    budibaseProxyService
        .validateConsultantRequest(jsonBody.toString(), method, request);
  }

  private void executeValidateUserRequest(HttpMethod method) {
    budibaseProxyService.validateUserRequest(getEmptyBody(), method, request);
  }

  private String getRequestBody(String userId) {
    JSONObject jsonBody = new JSONObject();
    JSONObject params = new JSONObject();
    params.put("bb_user_id", userId);
    jsonBody.put("parameters", params);
    return jsonBody.toString();
  }

}
