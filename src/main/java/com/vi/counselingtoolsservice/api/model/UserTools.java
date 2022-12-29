package com.vi.counselingtoolsservice.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_tools")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTools {

  @Id
  @Column(name = "user_id")
  @Size(max = 255)
  private String userId;

  @Column(name = "tools")
  @Size(max = 255)
  private String tools;

}
