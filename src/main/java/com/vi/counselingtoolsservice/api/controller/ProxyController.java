package com.vi.counselingtoolsservice.api.controller;

import com.vi.counselingtoolsservice.api.service.budibase.BudibaseProxyService;
import com.vi.counselingtoolsservice.api.service.budibase.BusibaseProxyAuthService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  private final BudibaseProxyService budibaseProxyService;

  private final BusibaseProxyAuthService busibaseProxyAuthService;

  @RequestMapping("/api/**")
  public ResponseEntity intercept(@RequestBody(required = false) String body,
      HttpMethod method, HttpServletRequest request) {

    if (budibaseProxyService.isWhiteListed(request)) {
      return executeNonModifiedRequest(body, method, request);
    }

    String role = busibaseProxyAuthService
        .extractRolesOfCurrentUsers(request.getQueryString(), request.getHeader("cookie"));

    if (role.equals("admin")) {
      return executeNonModifiedRequest(body, method, request);
    } else if (role.equals("consultant")) {
      return executeConsultantRequest(body, method, request);
    } else if (role.equals("user")) {
      return executeUserRequest(body, method, request);
    }

    log.error(
        "User request is not whitelisted, neither the logged in user is of role admin, consultant, user");
    throw new NotAllowedException(
        "User request is not whitelisted, neither the logged in user is of role admin, consultant, user");
  }

  @RequestMapping("/api/global/configs")
  public ResponseEntity interceptAndRestrictResponseBody(@RequestBody(required = false) String body,
      HttpMethod method, HttpServletRequest request) {
    var budibaseResponse = executeNonModifiedRequest(body, method, request);
    return restrictResponseBody(budibaseResponse);
  }

  @RequestMapping("/api/global/configs/**")
  public ResponseEntity interceptAndExecuteNonModifiedRequest(@RequestBody(required = false) String body,
      HttpMethod method, HttpServletRequest request) {
    return executeNonModifiedRequest(body, method, request);
  }

  ResponseEntity restrictResponseBody(ResponseEntity budibaseResponse) {
    if (budibaseResponse.getBody() != null && budibaseResponse.getStatusCode().is2xxSuccessful()) {
      return RestrictedAttributesRemover.removeRestrictedAttributesFromResponse(budibaseResponse, "clientSecret");
    } else {
      return budibaseResponse;
    }
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
    budibaseProxyService.validateConsultantRequest(body, method, request);
    HttpHeaders headers = prepareHeadersForNonAdminUser(request);
    var consultantBudibaseResponse = execute(request, method, body, headers);
    if (request.getRequestURI().contains("api/global/self")) {
      String string = consultantBudibaseResponse.getBody().toString();
      return new ResponseEntity("{}", consultantBudibaseResponse.getHeaders(), consultantBudibaseResponse.getStatusCode());

    } else {
      return consultantBudibaseResponse;
    }
  }

  private ResponseEntity executeUserRequest(String body, HttpMethod method,
      HttpServletRequest request) {
    budibaseProxyService.validateUserRequest(body, method, request);
    HttpHeaders headers = prepareHeadersForNonAdminUser(request);
    var userBudibaseResponse = execute(request, method, body, headers);
    if (request.getRequestURI().contains("api/global/self")) {
      return new ResponseEntity("{}", userBudibaseResponse.getHeaders(), userBudibaseResponse.getStatusCode());
    } else {
      return userBudibaseResponse;
    }
  }

  private HttpHeaders prepareHeadersForNonAdminUser(HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if (headerName.equals("accept-encoding") || headerName.equals("cookie")) {
        headers.set("accept-encoding", "identity");
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
      throw new BadRequestException();
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

}
