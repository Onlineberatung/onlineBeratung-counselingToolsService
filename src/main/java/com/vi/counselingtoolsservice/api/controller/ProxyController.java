package com.vi.counselingtoolsservice.api.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
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

  @RequestMapping("/api/**")
  public ResponseEntity intercept(@RequestBody(required = false) String body,
      HttpMethod method, HttpServletRequest request) {
    String role = extractRolesOfCurrentUsers(request);
    if (role.equals("admin")) {
      return executeAdminRequest(body, method, request);
    } else if (role.equals("consultant")) {
      return executeConsultantRequest(body, method, request);
    } else if (role.equals("user")) {
      return executeUserRequest(body, method, request);
    }

    throw new IllegalStateException("Unhandled call: TODO: log request");

  }

  private ResponseEntity executeAdminRequest(String body, HttpMethod method,
      HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if (headerName.equals("accept-encoding")) {
        continue;
      }
      headers.set(headerName, request.getHeader(headerName));
    }
    return execute(request, method, body, headers);
  }

  private ResponseEntity executeConsultantRequest(String body, HttpMethod method,
      HttpServletRequest request) {
    HttpHeaders headers = prepareHeadersForNonAdminUser(request);
    //TODO: verify in body is userId assigned to given consultant
    // if not throw 403
    return execute(request, method, body, headers);
  }

  private ResponseEntity executeUserRequest(String body, HttpMethod method,
      HttpServletRequest request) {
    HttpHeaders headers = prepareHeadersForNonAdminUser(request);
    //TODO: take userId from JWT token and override the body request with this userId or just do
    // the validation
    return execute(request, method, body, headers);
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
