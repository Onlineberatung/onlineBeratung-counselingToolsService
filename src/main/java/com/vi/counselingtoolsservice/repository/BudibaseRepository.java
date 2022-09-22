package com.vi.counselingtoolsservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BudibaseRepository {

  private @Autowired
  NamedParameterJdbcTemplate budibaseDBNamedParameterTemplate;

}
