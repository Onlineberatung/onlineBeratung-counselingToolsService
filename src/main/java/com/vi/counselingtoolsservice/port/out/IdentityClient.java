package com.vi.counselingtoolsservice.port.out;

import com.vi.counselingtoolsservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;

public interface IdentityClient {
  KeycloakLoginResponseDTO loginUser(final String userName, final String password);
}
