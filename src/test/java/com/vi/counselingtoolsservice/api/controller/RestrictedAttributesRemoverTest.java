package com.vi.counselingtoolsservice.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RestrictedAttributesRemoverTest {

  String globalSelfResponse = "{\"_id\":\"ro_ta_users_us_779a88ce-8679-43e0-8188-5c8c3ffa2d5e\",\"_rev\":\"661-85f34be19f00acba34267dc5d10ee523\",\"tableId\":\"ta_users\",\"roleId\":\"BASIC\",\"createdAt\":1675546918788,\"email\":\"digi.consultant.1@digi.consultant.1.com\",\"provider\":\"https://app-staging.suchtberatung.digital/auth/realms/online-beratung\",\"providerType\":\"oidc\",\"firstName\":\"digi.consultant.1\",\"lastName\":\"digi.consultant.1\",\"thirdPartyProfile\":{\"sub\":\"779a88ce-8679-43e0-8188-5c8c3ffa2d5e\",\"email_verified\":true,\"name\":\"digi.consultant.1 digi.consultant.1\",\"groups\":[\"consultant\",\"default-roles-caritas-online-beratung\",\"budibase-basic\",\"offline_access\"],\"preferred_username\":\"enc.mruwo2jomnxw443vnr2gc3tufyyq....\",\"given_name\":\"digi.consultant.1\",\"family_name\":\"digi.consultant.1\",\"email\":\"adnan.alicic.ext+digi.consultant.1@virtual-identity.com\"},\"oauth2\":{\"accessToken\":\"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJaX0dyNzFPMkFEWUkwOTJndEJjOE5KTU9BdDZtZG5EdGtoZVdLRnhPYkR3In0.eyJleHAiOjE2Nzk1ODY4MzAsImlhdCI6MTY3OTU4NjUzMCwiYXV0aF90aW1lIjoxNjc5NTg2NTMwLCJqdGkiOiJlY2I0Y2IyZS1iYzJlLTQ4OGMtYmZkMC00NDMwZmM2MjA5ZWEiLCJpc3MiOiJodHRwczovL2FwcC1zdGFnaW5nLnN1Y2h0YmVyYXR1bmcuZGlnaXRhbC9hdXRoL3JlYWxtcy9vbmxpbmUtYmVyYXR1bmciLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNzc5YTg4Y2UtODY3OS00M2UwLTgxODgtNWM4YzNmZmEyZDVlIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYnVkaWJhc2UtY2xpZW50Iiwic2Vzc2lvbl9zdGF0ZSI6IjlmN2RhMjg5LTEzNTItNGM5Yi1hZWUxLTEwZDg5ZGMxZjQzOCIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cHM6Ly90b29scy1kZXYuc3VjaHRiZXJhdHVuZy5kaWdpdGFsIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJjb25zdWx0YW50IiwiZGVmYXVsdC1yb2xlcy1jYXJpdGFzLW9ubGluZS1iZXJhdHVuZyIsImJ1ZGliYXNlLWJhc2ljIiwib2ZmbGluZV9hY2Nlc3MiXX0sInJlc291cmNlX2FjY2VzcyI6eyJidWRpYmFzZS1jbGllbnQiOnsicm9sZXMiOlsiYmFzaWMiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIG9mZmxpbmVfYWNjZXNzIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiI5ZjdkYTI4OS0xMzUyLTRjOWItYWVlMS0xMGQ4OWRjMWY0MzgiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6ImRpZ2kuY29uc3VsdGFudC4xIGRpZ2kuY29uc3VsdGFudC4xIiwiZ3JvdXBzIjpbImNvbnN1bHRhbnQiLCJkZWZhdWx0LXJvbGVzLWNhcml0YXMtb25saW5lLWJlcmF0dW5nIiwiYnVkaWJhc2UtYmFzaWMiLCJvZmZsaW5lX2FjY2VzcyJdLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJlbmMubXJ1d28yam9tbnh3NDQzdm5yMmdjM3R1Znl5cS4uLi4iLCJnaXZlbl9uYW1lIjoiZGlnaS5jb25zdWx0YW50LjEiLCJmYW1pbHlfbmFtZSI6ImRpZ2kuY29uc3VsdGFudC4xIiwiZW1haWwiOiJhZG5hbi5hbGljaWMuZXh0K2RpZ2kuY29uc3VsdGFudC4xQHZpcnR1YWwtaWRlbnRpdHkuY29tIn0.HFdEHlc2VMf-4H96q062vhWYnhDShgL2-3I4v2U2lu1m_tqrrP_CLqmA9gBjShE34Nu6g3Z3-JviGYUKShQmRSnK8nFJ2PTwS5TQeInhZ7shWtalpvzzA3wEkLybyi-lMGiWhZbd9UWJ3cyFaf43IqFNu0qIW4iVVHOpkC8sBZLpcKxBZKV-2ISis76l3zAOlrLesrLq2cJlN3MAtBhnGw6EF-PhpQg9X50HDp2PV-EoZtclEElKcUoF6C7qaQhmzsK9N9LRfM5w-6Y16Mqa8ySknypkJM389prp_BPATxcndeOnnDUHJYQr0LqHC1skC0JLZhR2dwaGiRi3WPGMJQ\",\"refreshToken\":\"eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkZGYwYzhiZS00OTI3LTQxMDctYTEwYS0xMDgxZTU4MzRjMjUifQ.eyJpYXQiOjE2Nzk1ODY1MzAsImp0aSI6ImQ3YzRmMzkwLWE3MDEtNGQ0My05ZWVhLTgyNTI4MjJkZGRkYiIsImlzcyI6Imh0dHBzOi8vYXBwLXN0YWdpbmcuc3VjaHRiZXJhdHVuZy5kaWdpdGFsL2F1dGgvcmVhbG1zL29ubGluZS1iZXJhdHVuZyIsImF1ZCI6Imh0dHBzOi8vYXBwLXN0YWdpbmcuc3VjaHRiZXJhdHVuZy5kaWdpdGFsL2F1dGgvcmVhbG1zL29ubGluZS1iZXJhdHVuZyIsInN1YiI6Ijc3OWE4OGNlLTg2NzktNDNlMC04MTg4LTVjOGMzZmZhMmQ1ZSIsInR5cCI6Ik9mZmxpbmUiLCJhenAiOiJidWRpYmFzZS1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiOWY3ZGEyODktMTM1Mi00YzliLWFlZTEtMTBkODlkYzFmNDM4Iiwic2NvcGUiOiJvcGVuaWQgb2ZmbGluZV9hY2Nlc3MgZW1haWwgcHJvZmlsZSIsInNpZCI6IjlmN2RhMjg5LTEzNTItNGM5Yi1hZWUxLTEwZDg5ZGMxZjQzOCJ9.1lXc4zB24O7T6pJ0MKzByJtWV7BzyxJcA926H4n7VsA\"},\"tenantId\":\"default\",\"status\":\"active\",\"updatedAt\":\"2023-03-23T15:49:03.898Z\",\"dayPassRecordedAt\":\"2023-03-23T13:25:19.478Z\",\"forceResetPassword\":false,\"csrfToken\":\"efb2c51b-3b68-4e45-b0f0-8d1d1fbe67bb\",\"license\":{\"features\":[],\"quotas\":{\"usage\":{\"monthly\":{\"queries\":{\"name\":\"Queries\",\"value\":-1,\"triggers\":[]},\"automations\":{\"name\":\"Automations\",\"value\":-1,\"triggers\":[80,90,100]},\"dayPasses\":{\"name\":\"Day Passes\",\"value\":-1,\"triggers\":[80,90,100]}},\"static\":{\"rows\":{\"name\":\"Rows\",\"value\":-1,\"triggers\":[90,100]},\"apps\":{\"name\":\"Apps\",\"value\":-1,\"triggers\":[100]},\"userGroups\":{\"name\":\"User Groups\",\"value\":0,\"triggers\":[80,100]},\"plugins\":{\"name\":\"Plugins\",\"value\":10,\"triggers\":[90,100]}}},\"constant\":{\"automationLogRetentionDays\":{\"name\":\"Automation Logs\",\"value\":1,\"triggers\":[]},\"appBackupRetentionDays\":{\"name\":\"App Backups\",\"value\":0,\"triggers\":[]}}},\"plan\":{\"type\":\"free\"},\"refreshedAt\":\"2023-03-23T15:48:43.884Z\",\"version\":\"2.2.22\"}}";
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

  @Test
  void removeRestrictedAttributesFromResponseAsMap_Should_FilterResponse() {

    // given
    ResponseEntity budibaseResponse = new ResponseEntity(globalSelfResponse, HttpStatus.OK);

    // when
    ResponseEntity responseEntity = RestrictedAttributesRemover.removeRestrictedAttributesFromResponseAsMap(budibaseResponse);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(budibaseResponse.getStatusCode());
    assertThat(responseEntity.getBody().toString()).doesNotContain("email");
    assertThat(responseEntity.getBody().toString()).doesNotContain("_id");
  }

  @Test
  void removeRestrictedAttributesFromResponseAsMap_Should_ReturnUnfilteredResponse() {

    // given
    ResponseEntity budibaseResponse = new ResponseEntity(globalSelfResponse, HttpStatus.OK);

    // when
    ResponseEntity responseEntity = RestrictedAttributesRemover.removeRestrictedAttributesFromResponseAsMap(budibaseResponse);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(budibaseResponse.getStatusCode());
    assertThat(responseEntity.getBody().toString()).doesNotContain("_id");
    assertThat(responseEntity.getBody().toString()).doesNotContain("email");
  }
}