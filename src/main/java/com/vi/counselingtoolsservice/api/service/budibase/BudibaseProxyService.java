package com.vi.counselingtoolsservice.api.service.budibase;

import com.vi.counselingtoolsservice.budibaseApi.generated.ApiClient;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.DefaultApi;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.App;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.AssignToolsRequest;
import com.vi.counselingtoolsservice.budibaseApi.generated.web.model.User;
import com.vi.counselingtoolsservice.config.CacheManagerConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class BudibaseProxyService {

  @Value("${budibase.proxy.host}")
  private String proxyServiceHost;

  @Value("${budibase.proxy.port}")
  private Integer proxyServicePort;


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
}
