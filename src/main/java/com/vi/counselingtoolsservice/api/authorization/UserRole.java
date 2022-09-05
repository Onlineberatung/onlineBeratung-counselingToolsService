package com.vi.counselingtoolsservice.api.authorization;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum UserRole {

  ANONYMOUS("anonymous"),
  USER("user"),
  CONSULTANT("consultant"),
  TECHNICAL("technical"),
  PEER_CONSULTANT("peer-consultant"),
  MAIN_CONSULTANT("main-consultant"),
  GROUP_CHAT_CONSULTANT("group-chat-consultant"),
  USER_ADMIN("user-admin"),
  SINGLE_TENANT_ADMIN("single-tenant-admin"),
  TENANT_ADMIN("tenant-admin");

  private final String value;

  public static Optional<UserRole> getRoleByValue(String value) {
    return Arrays.stream(values()).filter(userRole -> userRole.value.equals(value)).findFirst();
  }
}
