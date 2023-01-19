package com.vi.counselingtoolsservice.api.controller;

import com.vi.counselingtoolsservice.api.service.budibase.BudibaseApiService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@Slf4j
@RequiredArgsConstructor
public class ProxyController {

  @Value("${budibase.proxy.host}")
  private String proxyServiceHost;

  @Value("${budibase.proxy.port}")
  private Integer proxyServicePort;

  @Value("${budibase.api.key}")
  private String budibaseApiKey;

  private final BudibaseApiService budibaseApiService;

  @RequestMapping("/api/**")
  public ResponseEntity intercept(@RequestBody(required = false) String body,
      HttpMethod method, HttpServletRequest request) {

    if (isUnprotectedEndpoint(request)) {
      return executeNonModifiedRequest(body, method, request);
    }

    String role = extractRolesOfCurrentUsers(request);
    if (role.equals("admin")) {
      return executeNonModifiedRequest(body, method, request);
    } else if (role.equals("consultant")) {
      return executeConsultantRequest(body, method, request);
    } else if (role.equals("user")) {
      return executeUserRequest(body, method, request);
    }

    throw new IllegalStateException("Unhandled call: TODO: log request");

  }

  private boolean isUnprotectedEndpoint(HttpServletRequest request) {
    String uri = request.getRequestURI();
    //TODO: Speak with Simon to add also the configs table here, hence all tables that don't have user data
    return
        //TODO: needs to be excluded from here
        uri.contains("__apps")
            || uri.contains("api/tables/ta_users")
            || uri.contains("__user/search")
            || uri.contains("__tools_consultant_access/search")
            || uri.contains("__tools_documentation/search")
        ;

    //TODO: get decoded sessions needs to be refactored in the app. its catched for now with api/v2/queries
  }

  private ResponseEntity executeNonModifiedRequest(String body, HttpMethod method,
      HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if (headerName.equals("accept-encoding")) {
        headers.set("accept-encoding", "identity");
        continue;
      }
      headers.set(headerName, request.getHeader(headerName));
    }
    return execute(request, method, body, headers);
  }

  private ResponseEntity executeConsultantRequest(String body, HttpMethod method,
      HttpServletRequest request) {
    String consultantId = extractUserIdFromJWT(request);
    List<String> consultantAssignedUsers = budibaseApiService
        .getConsultantAssignedUsers(consultantId);
    String userId = extractUserIdFromBodyReadOperation(body);
    Optional<String> matchedUserId = consultantAssignedUsers.stream()
        .filter(el -> el.equals(userId)).findFirst();
    if (matchedUserId.isEmpty()) {
      throw new NotAllowedException(
          "You don't have permissions to access user specific data for user with id: " + userId
      );
    }

    HttpHeaders headers = prepareHeadersForNonAdminUser(request);
    return execute(request, method, body, headers);
  }

  private ResponseEntity executeUserRequest(String body, HttpMethod method,
      HttpServletRequest request) {

    String userIdJWT = extractUserIdFromJWT(request);
    String userIdBody;
    if (HttpMethod.GET.equals(method)) {
      userIdBody = extractUserIdFromBodyReadOperation(body);
    } else if (HttpMethod.POST.equals(method)) {
      userIdBody = extractUserIdFromBodyUpdateOperation(body);
    } else {
      throw new IllegalStateException("Unsupported HTTP method for user request");
    }

    if (!userIdJWT.equals(userIdBody)) {
      throw new NotAllowedException(
          "You are not allowed to access data for user_id " + userIdBody);
    }

    HttpHeaders headers = prepareHeadersForNonAdminUser(request);
    return execute(request, method, body, headers);
  }


  private String extractUserIdFromBodyReadOperation(String body) {
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

  private String extractUserIdFromBodyUpdateOperation(String body) {
    JSONObject bodyJSONObject = new JSONObject(body);
    return (String) bodyJSONObject.get("bb_user_id");
  }

  private String extractUserIdFromJWT(HttpServletRequest request) {
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

  private HttpHeaders prepareHeadersForNonAdminUser(HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if (headerName.equals("accept-encoding") || headerName.equals("cookie")) {
        continue;
      }
      headers.set(headerName, request.getHeader(headerName));
    }

    headers.set("x-budibase-api-key", budibaseApiKey);
    return headers;
  }

  private ResponseEntity execute(HttpServletRequest request, HttpMethod method, String body,
      HttpHeaders headers) {
    String requestUrl = request.getRequestURI();
    URI uri = null;
    try {
      uri = new URI("http", null, proxyServiceHost, proxyServicePort, null, null, null);
      uri = UriComponentsBuilder.fromUri(uri)
          .path(requestUrl)
          .query(request.getQueryString())
          .build(true).toUri();
    } catch (URISyntaxException e) {
      // TODO: log me
    }

    HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
    RestTemplate restTemplate = new RestTemplate();
    try {
      return restTemplate.exchange(uri, method, httpEntity, String.class);
    } catch (HttpStatusCodeException e) {
      return ResponseEntity.status(e.getRawStatusCode())
          .headers(e.getResponseHeaders())
          .body(e.getResponseBodyAsString());
    }
  }

  private String extractRolesOfCurrentUsers(HttpServletRequest request) {
    //TODO: this call validates also is the user logged in

    URI uri = null;
    try {
      uri = new URI("http", null, proxyServiceHost, proxyServicePort, null, null, null);
      uri = UriComponentsBuilder.fromUri(uri)
          .path("api/global/self")
          .query(request.getQueryString())
          .build(true).toUri();
    } catch (URISyntaxException e) {
      // TODO: log me
    }

    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.add("cookie", request.getHeader("cookie"));
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

}
