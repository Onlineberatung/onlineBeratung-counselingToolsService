package com.vi.counselingtoolsservice.api.service.budibase;

import com.vi.counselingtoolsservice.config.CacheManagerConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@RequiredArgsConstructor
public class BudibaseProxyService {

  @Value("${budibase.proxy.host}")
  private String proxyServiceHost;

  @Value("${budibase.proxy.port}")
  private Integer proxyServicePort;

  @Value("${budibase.proxy.whitelisted}")
  private String whitelistedURIs;

  private final BudibaseApiService budibaseApiService;

  public boolean isWhiteListed(HttpServletRequest request) {
    Set<String> whitelistedURIs = new HashSet<>(Arrays.asList(this.whitelistedURIs.split(";")));
    return whitelistedURIs.contains(request.getRequestURI());
  }

  public String extractUserIdFromJWT(HttpServletRequest request) {
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
    List<String> consultantAssignedUsers = budibaseApiService
        .getConsultantAssignedUsers(consultantId);
    String userId = extractUserIdFromBodyReadOperation(method, body);
    Optional<String> matchedUserId = consultantAssignedUsers.stream()
        .filter(el -> el.equals(userId)).findFirst();
    if (matchedUserId.isEmpty()) {
      throw new NotAllowedException(
          "You don't have permissions to access user specific data for user with id: " + userId
      );
    }
  }


  @Cacheable(cacheNames = CacheManagerConfig.TOKEN_CACHE)
  public String extractRolesOfCurrentUsers(String queryStirng, String cookie) {
    //TODO: this call validates also is the user logged in

    URI uri = null;
    try {
      uri = new URI("http", null, proxyServiceHost, proxyServicePort, null, null, null);
      uri = UriComponentsBuilder.fromUri(uri)
          .path("api/global/self")
          .query(queryStirng)
          .build(true).toUri();
    } catch (URISyntaxException e) {
      // TODO: log me
    }

    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.add("cookie", cookie);
    headers.add("accept", "application/json");
    headers.add("content-type", "application/json");
    headers.add("accept-encoding", "utf-8");
    HttpEntity<String> httpEntity = new HttpEntity<>(headers);
    ResponseEntity<String> exchange = restTemplate
        .exchange(uri, HttpMethod.GET, httpEntity, String.class);
    String response = exchange.getBody();
    boolean isAdmin = isUserAdmin(response);
    if (isAdmin) {
      return "admin";
    } else {
      return identifyNonAdminUser(response);
    }
  }

  private String identifyNonAdminUser(String response) {
    JSONObject jsonObject = new JSONObject(response);
    JSONObject thirdPartyProfile = (JSONObject) jsonObject.get("thirdPartyProfile");
    JSONArray roles = ((JSONArray) thirdPartyProfile.get("groups"));
    Iterator<Object> iterator = roles.iterator();
    while (iterator.hasNext()) {
      String next = (String) iterator.next();
      if (next.equals("user") || next.equals("consultant")) {
        return next;
      }
    }
    throw new IllegalStateException(
        "User is not in one of the following categories [user, consultant, admin]");
  }

  private boolean isUserAdmin(String response) {
    JSONObject jsonObject = new JSONObject(response);
    try {
      Object isGlobal = ((JSONObject) jsonObject.get("admin")).get("global");
      return (boolean) isGlobal;
    } catch (Exception e) {
      return false;
    }
  }

  private String extractUserIdFromBodyReadOperation(HttpMethod method, String body) {

    if (method.equals(HttpMethod.GET)) {
      throw new IllegalStateException();
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
    if (HttpMethod.POST.equals(method) && request.getRequestURI().contains("rows")) {
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
