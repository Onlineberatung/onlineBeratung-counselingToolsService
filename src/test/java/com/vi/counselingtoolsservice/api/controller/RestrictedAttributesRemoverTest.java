package com.vi.counselingtoolsservice.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RestrictedAttributesRemoverTest {

  String globalSelfResponse = "{\n"
      + "  \"_id\": \"us_cdbd0945e6e545bb913434d4610d7bcb\",\n"
      + "  \"_rev\": \"5-1b48238e596fa12e5cd32b4261de95ce\",\n"
      + "  \"createdAt\": 1677708379468,\n"
      + "  \"email\": \"admin@admin.de\",\n"
      + "  \"roles\": {\n"
      + "    \n"
      + "  },\n"
      + "  \"builder\": {\n"
      + "    \"global\": true\n"
      + "  },\n"
      + "  \"admin\": {\n"
      + "    \"global\": true\n"
      + "  },\n"
      + "  \"tenantId\": \"default\",\n"
      + "  \"status\": \"active\",\n"
      + "  \"updatedAt\": \"2023-03-23T10:32:54.398Z\",\n"
      + "  \"dayPassRecordedAt\": \"2023-03-23T10:32:54.397Z\",\n"
      + "  \"featureFlags\": [\n"
      + "    \"LICENSING\",\n"
      + "    \"USER_GROUPS\"\n"
      + "  ],\n"
      + "  \"license\": {\n"
      + "    \"features\": [\n"
      + "      \n"
      + "    ],\n"
      + "    \"quotas\": {\n"
      + "      \"usage\": {\n"
      + "        \"monthly\": {\n"
      + "          \"queries\": {\n"
      + "            \"name\": \"Queries\",\n"
      + "            \"value\": -1\n"
      + "          },\n"
      + "          \"automations\": {\n"
      + "            \"name\": \"Automations\",\n"
      + "            \"value\": -1\n"
      + "          },\n"
      + "          \"dayPasses\": {\n"
      + "            \"name\": \"Day Passes\",\n"
      + "            \"value\": -1\n"
      + "          }\n"
      + "        },\n"
      + "        \"static\": {\n"
      + "          \"rows\": {\n"
      + "            \"name\": \"Rows\",\n"
      + "            \"value\": -1\n"
      + "          },\n"
      + "          \"apps\": {\n"
      + "            \"name\": \"Apps\",\n"
      + "            \"value\": -1\n"
      + "          },\n"
      + "          \"userGroups\": {\n"
      + "            \"name\": \"User Groups\",\n"
      + "            \"value\": 0\n"
      + "          },\n"
      + "          \"plugins\": {\n"
      + "            \"name\": \"Plugins\",\n"
      + "            \"value\": 10\n"
      + "          }\n"
      + "        }\n"
      + "      },\n"
      + "      \"constant\": {\n"
      + "        \"automationLogRetentionDays\": {\n"
      + "          \"name\": \"Automation Logs\",\n"
      + "          \"value\": 1\n"
      + "        }\n"
      + "      }\n"
      + "    },\n"
      + "    \"plan\": {\n"
      + "      \"type\": \"free\"\n"
      + "    },\n"
      + "    \"refreshedAt\": \"2023-03-23T13:13:16.171Z\"\n"
      + "  },\n"
      + "  \"budibaseAccess\": true,\n"
      + "  \"accountPortalAccess\": false\n"
      + "}";
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
    assertThat(responseEntity.getBody()).hasToString("{\n"
        + "  \n"
        + "  \"_rev\": \"5-1b48238e596fa12e5cd32b4261de95ce\",\n"
        + "  \"createdAt\": 1677708379468,\n"
        + "  \n"
        + "  \"roles\": {\n"
        + "    \n"
        + "  },\n"
        + "  \"builder\": {\n"
        + "    \"global\": true\n"
        + "  },\n"
        + "  \"admin\": {\n"
        + "    \"global\": true\n"
        + "  },\n"
        + "  \"tenantId\": \"default\",\n"
        + "  \"status\": \"active\",\n"
        + "  \"updatedAt\": \"2023-03-23T10:32:54.398Z\",\n"
        + "  \"dayPassRecordedAt\": \"2023-03-23T10:32:54.397Z\",\n"
        + "  \"featureFlags\": [\n"
        + "    \"LICENSING\",\n"
        + "    \"USER_GROUPS\"\n"
        + "  ],\n"
        + "  \"license\": {\n"
        + "    \"features\": [\n"
        + "      \n"
        + "    ],\n"
        + "    \"quotas\": {\n"
        + "      \"usage\": {\n"
        + "        \"monthly\": {\n"
        + "          \"queries\": {\n"
        + "            \"name\": \"Queries\",\n"
        + "            \"value\": -1\n"
        + "          },\n"
        + "          \"automations\": {\n"
        + "            \"name\": \"Automations\",\n"
        + "            \"value\": -1\n"
        + "          },\n"
        + "          \"dayPasses\": {\n"
        + "            \"name\": \"Day Passes\",\n"
        + "            \"value\": -1\n"
        + "          }\n"
        + "        },\n"
        + "        \"static\": {\n"
        + "          \"rows\": {\n"
        + "            \"name\": \"Rows\",\n"
        + "            \"value\": -1\n"
        + "          },\n"
        + "          \"apps\": {\n"
        + "            \"name\": \"Apps\",\n"
        + "            \"value\": -1\n"
        + "          },\n"
        + "          \"userGroups\": {\n"
        + "            \"name\": \"User Groups\",\n"
        + "            \"value\": 0\n"
        + "          },\n"
        + "          \"plugins\": {\n"
        + "            \"name\": \"Plugins\",\n"
        + "            \"value\": 10\n"
        + "          }\n"
        + "        }\n"
        + "      },\n"
        + "      \"constant\": {\n"
        + "        \"automationLogRetentionDays\": {\n"
        + "          \"name\": \"Automation Logs\",\n"
        + "          \"value\": 1\n"
        + "        }\n"
        + "      }\n"
        + "    },\n"
        + "    \"plan\": {\n"
        + "      \"type\": \"free\"\n"
        + "    },\n"
        + "    \"refreshedAt\": \"2023-03-23T13:13:16.171Z\"\n"
        + "  },\n"
        + "  \"budibaseAccess\": true,\n"
        + "  \"accountPortalAccess\": false\n"
        + "}");
  }

  @Test
  void removeRestrictedAttributesFromResponseAsMap_Should_ReturnUnfilteredResponse() {

    // given
    ResponseEntity budibaseResponse = new ResponseEntity(globalSelfResponse, HttpStatus.OK);

    // when
    ResponseEntity responseEntity = RestrictedAttributesRemover.removeRestrictedAttributesFromResponseAsMap(budibaseResponse);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(budibaseResponse.getStatusCode());
    assertThat(responseEntity.getBody()).isEqualTo("{\n"
        + "  \n"
        + "  \"_rev\": \"5-1b48238e596fa12e5cd32b4261de95ce\",\n"
        + "  \"createdAt\": 1677708379468,\n"
        + "  \n"
        + "  \"roles\": {\n"
        + "    \n"
        + "  },\n"
        + "  \"builder\": {\n"
        + "    \"global\": true\n"
        + "  },\n"
        + "  \"admin\": {\n"
        + "    \"global\": true\n"
        + "  },\n"
        + "  \"tenantId\": \"default\",\n"
        + "  \"status\": \"active\",\n"
        + "  \"updatedAt\": \"2023-03-23T10:32:54.398Z\",\n"
        + "  \"dayPassRecordedAt\": \"2023-03-23T10:32:54.397Z\",\n"
        + "  \"featureFlags\": [\n"
        + "    \"LICENSING\",\n"
        + "    \"USER_GROUPS\"\n"
        + "  ],\n"
        + "  \"license\": {\n"
        + "    \"features\": [\n"
        + "      \n"
        + "    ],\n"
        + "    \"quotas\": {\n"
        + "      \"usage\": {\n"
        + "        \"monthly\": {\n"
        + "          \"queries\": {\n"
        + "            \"name\": \"Queries\",\n"
        + "            \"value\": -1\n"
        + "          },\n"
        + "          \"automations\": {\n"
        + "            \"name\": \"Automations\",\n"
        + "            \"value\": -1\n"
        + "          },\n"
        + "          \"dayPasses\": {\n"
        + "            \"name\": \"Day Passes\",\n"
        + "            \"value\": -1\n"
        + "          }\n"
        + "        },\n"
        + "        \"static\": {\n"
        + "          \"rows\": {\n"
        + "            \"name\": \"Rows\",\n"
        + "            \"value\": -1\n"
        + "          },\n"
        + "          \"apps\": {\n"
        + "            \"name\": \"Apps\",\n"
        + "            \"value\": -1\n"
        + "          },\n"
        + "          \"userGroups\": {\n"
        + "            \"name\": \"User Groups\",\n"
        + "            \"value\": 0\n"
        + "          },\n"
        + "          \"plugins\": {\n"
        + "            \"name\": \"Plugins\",\n"
        + "            \"value\": 10\n"
        + "          }\n"
        + "        }\n"
        + "      },\n"
        + "      \"constant\": {\n"
        + "        \"automationLogRetentionDays\": {\n"
        + "          \"name\": \"Automation Logs\",\n"
        + "          \"value\": 1\n"
        + "        }\n"
        + "      }\n"
        + "    },\n"
        + "    \"plan\": {\n"
        + "      \"type\": \"free\"\n"
        + "    },\n"
        + "    \"refreshedAt\": \"2023-03-23T13:13:16.171Z\"\n"
        + "  },\n"
        + "  \"budibaseAccess\": true,\n"
        + "  \"accountPortalAccess\": false\n"
        + "}");
  }
}