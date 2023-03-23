package com.vi.counselingtoolsservice.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.counselingtoolsservice.util.JsonSerializationUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.NotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Slf4j
public class RestrictedAttributesRemover {

  public static final String CLIENT_SECRET = "clientSecret";

  private RestrictedAttributesRemover() {

  }

  static ResponseEntity removeRestrictedAttributesFromResponse(
      ResponseEntity budibaseResponse, String attributeName) {
    if (budibaseResponse.getBody() == null) {
      log.warn("Budibase response was null");
      return budibaseResponse;
    } else {
      var responseString = budibaseResponse.getBody().toString();
      try {
        List<Map<String, Object>> list = JsonSerializationUtils.deserializeFromJsonString(
            responseString, List.class);
        list.stream().forEach(map -> removeRecursively(map, attributeName));
        String result = new ObjectMapper().writeValueAsString(list);
        return new ResponseEntity(result,
            budibaseResponse.getHeaders(), budibaseResponse.getStatusCode());
      } catch (JsonProcessingException e) {
        log.error("Budibase response was not parseable to string");
        throw new NotAllowedException(
            "Budibase API returned non-parsable json object, handling it as access denied");
      }
    }
  }

  static ResponseEntity removeRestrictedAttributesFromResponseAsMap(
      ResponseEntity budibaseResponse, String... attributesNames) {
    if (budibaseResponse.getBody() == null) {
      log.warn("Budibase response was null");
      return budibaseResponse;
    } else {
      var responseString = budibaseResponse.getBody().toString();
      try {
        Map<String, Object> map = JsonSerializationUtils.deserializeFromJsonString(responseString,
            Map.class);
        Arrays.stream(attributesNames)
            .forEach(attributeName -> removeRecursively(map, attributeName));
        String result = new ObjectMapper().writeValueAsString(map);
        return new ResponseEntity(result,
            budibaseResponse.getHeaders(), budibaseResponse.getStatusCode());
      } catch (JsonProcessingException e) {
        log.error("Budibase response was not parseable to string");
        throw new NotAllowedException(
            "Budibase API returned non-parsable json object, handling it as access denied");
      }
    }
  }

  private static void removeElementFromMap(Object elem, String attribute) {
    if (elem instanceof Map) {
      removeRecursively((Map) elem, attribute);
    }
    if (elem instanceof List) {
      ((List<?>) elem).forEach(x -> removeElementFromMap(x, attribute));
    }
  }

  private static void removeRecursively(Map<String, Object> map, String attribute) {
    map.values().forEach(elem -> removeElementFromMap(elem, attribute));
    map.remove(attribute);
  }
}
