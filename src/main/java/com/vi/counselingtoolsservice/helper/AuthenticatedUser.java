package com.vi.counselingtoolsservice.helper;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vi.counselingtoolsservice.api.authorization.UserRole;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Representation of the via Keycloak authenticated user
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthenticatedUser {

  @NonNull
  private String userId;

  @NonNull
  private String username;

  private Set<String> roles;

  @NonNull
  private String accessToken;

  private Set<String> grantedAuthorities;

  @JsonIgnore
  public boolean isAdviceSeeker() {
    return nonNull(roles) && roles.contains(UserRole.USER.getValue());
  }

  @JsonIgnore
  public boolean isConsultant() {
    return nonNull(roles) && roles.contains(UserRole.CONSULTANT.getValue());
  }

  @JsonIgnore
  public boolean isSingleTenantAdmin() {
    return nonNull(roles) && roles.contains(UserRole.SINGLE_TENANT_ADMIN.getValue());
  }

  @JsonIgnore
  public boolean isTenantSuperAdmin() {
    return nonNull(roles) && roles.contains(UserRole.TENANT_ADMIN.getValue());
  }

  @JsonIgnore
  public boolean isAnonymous() {
    return nonNull(roles) && roles.contains(UserRole.ANONYMOUS.getValue());
  }
}
