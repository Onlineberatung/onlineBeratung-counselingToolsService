package com.vi.counselingtoolsservice.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RestrictedAttributesRemoverTest {

  @Test
  void restrictResponseBody_Should_FilterResponse() {

    // given
    String json = "[{\"_id\":\"config_oidc\",\"_rev\":\"_revId\",\"type\":\"oidc\",\"config\":{\"configs\":[{\"activated\":true,\"scopes\":[\"profile\",\"email\",\"offline_access\"],\"logo\":null,\"name\":\"\",\"clientSecret\":\"someSecretValue\",\"clientID\":\"budibase-client\",\"configUrl\":\"https://some-example-url/auth/realms/online-beratung/.well-known/openid-configuration\",\"uuid\":\"someuuid\"}]},\"createdAt\":\"2023-01-30T05:59:09.816Z\",\"updatedAt\":\"2023-01-30T05:59:09.816Z\"},{\"_id\":\"config_settings\",\"_rev\":\"3-67418869da83860c4d1d6f3600c451c0\",\"type\":\"settings\",\"config\":{\"platformUrl\":\"https://some-example-url\",\"logoUrl\":\"\",\"company\":\"Budibase\",\"analyticsEnabled\":true,\"uniqueTenantId\":\"f72dd0e67aa64467acd340684fe976dc_default\",\"_rev\":\"2-38fac82caff235b98cabf7122dde3579\"},\"createdAt\":\"2023-01-26T06:43:44.151Z\",\"updatedAt\":\"2023-01-26T06:43:44.151Z\"}]";
    ResponseEntity budibaseResponse = new ResponseEntity(json, HttpStatus.OK);

    // when
    ResponseEntity responseEntity = RestrictedAttributesRemover.removeRestrictedAttributesFromResponse(budibaseResponse, "clientSecret");

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(budibaseResponse.getStatusCode());
    assertThat(responseEntity.getBody().toString()).doesNotContain("clientSecret");
    assertThat(responseEntity.getBody()).hasToString("[{\"_id\":\"config_oidc\",\"_rev\":\"_revId\",\"type\":\"oidc\",\"config\":{\"configs\":[{\"activated\":true,\"scopes\":[\"profile\",\"email\",\"offline_access\"],\"logo\":null,\"name\":\"\",\"clientID\":\"budibase-client\",\"configUrl\":\"https://some-example-url/auth/realms/online-beratung/.well-known/openid-configuration\",\"uuid\":\"someuuid\"}]},\"createdAt\":\"2023-01-30T05:59:09.816Z\",\"updatedAt\":\"2023-01-30T05:59:09.816Z\"},{\"_id\":\"config_settings\",\"_rev\":\"3-67418869da83860c4d1d6f3600c451c0\",\"type\":\"settings\",\"config\":{\"platformUrl\":\"https://some-example-url\",\"logoUrl\":\"\",\"company\":\"Budibase\",\"analyticsEnabled\":true,\"uniqueTenantId\":\"f72dd0e67aa64467acd340684fe976dc_default\",\"_rev\":\"2-38fac82caff235b98cabf7122dde3579\"},\"createdAt\":\"2023-01-26T06:43:44.151Z\",\"updatedAt\":\"2023-01-26T06:43:44.151Z\"}]");
  }

  @Test
  void restrictResponseBody_Should_ReturnUnfilteredResponseIfResponseStringDoesNotHaveClientSecret() {

    // given
    String originalJsonWithoutClientSecret = "[{\"_id\":\"config_oidc\",\"_rev\":\"_revId\",\"type\":\"oidc\",\"config\":{\"configs\":[{\"activated\":true,\"scopes\":[\"profile\",\"email\",\"offline_access\"],\"logo\":null,\"name\":\"\",\"clientID\":\"budibase-client\",\"configUrl\":\"https://some-example-url/auth/realms/online-beratung/.well-known/openid-configuration\",\"uuid\":\"someuuid\"}]},\"createdAt\":\"2023-01-30T05:59:09.816Z\",\"updatedAt\":\"2023-01-30T05:59:09.816Z\"},{\"_id\":\"config_settings\",\"_rev\":\"3-67418869da83860c4d1d6f3600c451c0\",\"type\":\"settings\",\"config\":{\"platformUrl\":\"https://some-example-url\",\"logoUrl\":\"\",\"company\":\"Budibase\",\"analyticsEnabled\":true,\"uniqueTenantId\":\"f72dd0e67aa64467acd340684fe976dc_default\",\"_rev\":\"2-38fac82caff235b98cabf7122dde3579\"},\"createdAt\":\"2023-01-26T06:43:44.151Z\",\"updatedAt\":\"2023-01-26T06:43:44.151Z\"}]";
    ResponseEntity budibaseResponse = new ResponseEntity(originalJsonWithoutClientSecret, HttpStatus.OK);

    // when
    ResponseEntity responseEntity = RestrictedAttributesRemover.removeRestrictedAttributesFromResponse(budibaseResponse, "clientSecret");

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(budibaseResponse.getStatusCode());
    assertThat(responseEntity.getBody().toString()).doesNotContain("clientSecret");
    assertThat(responseEntity.getBody()).hasToString(originalJsonWithoutClientSecret);
  }
}