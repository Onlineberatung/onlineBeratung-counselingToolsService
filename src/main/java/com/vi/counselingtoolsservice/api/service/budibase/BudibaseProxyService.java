package com.vi.counselingtoolsservice.api.service.budibase;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BudibaseProxyService {

  @Value("${budibase.proxy.whitelisted}")
  private String whitelistedURIs;

  private final BudibaseApiService budibaseApiService;

  public boolean isWhiteListed(HttpServletRequest request) {
    Set<String> whitelistedURIs = new HashSet<>(Arrays.asList(this.whitelistedURIs.split(";")));
    Optional<String> match = whitelistedURIs.stream()
        .filter(el -> request.getRequestURI().contains(el)).findFirst();
    return match.isPresent();
  }

  protected String extractUserIdFromJWT(HttpServletRequest request) {
    Base64.Decoder decoder = Base64.getUrlDecoder();

    String[] cookies = request.getHeader("cookie").split(";");

    Optional<String> authCookie = Arrays.stream(cookies)
        .filter(cookie -> cookie.contains("budibase:auth") && !cookie.equals("budibase:auth="))
        .findFirst();

    String token = authCookie.get().split("=")[1];
    String[] chunks = token.split("\\.");
    String payload = new String(decoder.decode(chunks[1]));
    JSONObject jsonObject = new JSONObject(payload);
    String budibaseUserId = (String) jsonObject.get("userId");
    return budibaseUserId.substring(3);
  }

  public void validateConsultantRequest(String body, HttpMethod method,
      HttpServletRequest request) {
    String consultantId = extractUserIdFromJWT(request);

    String userId;
    if (request.getRequestURI().contains("api/v2/queries")) {
      userId = extractUserIdFromV2Query(body);
    } else {
      userId = extractUserIdFromBodyReadOperation(method, body);
    }

    if (consultantId.equals(userId)) {
      return;
    }

    List<String> consultantAssignedUsers = budibaseApiService
        .getConsultantAssignedUsers(consultantId);
    Optional<String> matchedUserId = consultantAssignedUsers.stream()
        .filter(el -> el.equals(userId)).findFirst();
    if (matchedUserId.isEmpty()) {
      throw new NotAllowedException(
          "You don't have permissions to access user specific data for user with id: " + userId
      );
    }
  }

  private String extractUserIdFromV2Query(String body) {
    JSONObject bodyJSONObject = new JSONObject(body);
    JSONObject parameters = (JSONObject) bodyJSONObject.get("parameters");
    return (String) parameters.get("bb_user_id");
  }

  private String extractUserIdFromBodyReadOperation(HttpMethod method, String body) {

    if (method.equals(HttpMethod.GET)) {
      throw new NotAllowedException("You are not allowed to access data.");
    }
    JSONObject bodyJSONObject = new JSONObject(body);
    JSONObject query = (JSONObject) bodyJSONObject.get("query");
    JSONObject equalOperation = (JSONObject) query.get("equal");

    Map<String, Object> filters = equalOperation.toMap();
    Optional<String> filterName = filters.keySet().stream().filter(el -> el.contains("user_id"))
        .findFirst();

    if (filterName.isEmpty()) {
      throw new BadRequestException(
          "You should not be able to reach this point, hence in case it's a datasource without user_id than it should be filtered out before");
    }

    return (String) filters.get(filterName.get());
  }

  public void validateUserRequest(String body, HttpMethod method, HttpServletRequest request) {
    String userIdJWT = extractUserIdFromJWT(request);
    String userIdBody;

    if (request.getRequestURI().contains("api/v2/queries")) {
      userIdBody = extractUserIdFromV2Query(body);
    } else if (HttpMethod.POST.equals(method) && request.getRequestURI().contains("rows")) {
      userIdBody = extractUserIdFromBodyUpdateOperation(body);
    } else {
      userIdBody = extractUserIdFromBodyReadOperation(method, body);
    }

    if (!userIdJWT.equals(userIdBody)) {
      throw new NotAllowedException(
          "You are not allowed to access data for user_id " + userIdBody);
    }

  }

  private String extractUserIdFromBodyUpdateOperation(String body) {
    JSONObject bodyJSONObject = new JSONObject(body);
    return (String) bodyJSONObject.get("bb_user_id");
  }


}
