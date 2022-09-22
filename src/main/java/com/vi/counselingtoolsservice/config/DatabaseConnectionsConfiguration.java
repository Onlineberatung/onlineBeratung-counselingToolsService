package com.vi.counselingtoolsservice.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class DatabaseConnectionsConfiguration {

  @Value("${budibase.database.url}")
  private String budibaseDatabaseUrl;

  @Value("${budibase.database.username}")
  private String budibaseDatabaseUsername;

  @Value("${budibase.database.password}")
  private String budibaseDatabasePassword;

  @Value("${budibase.database.driverClass}")
  private String budibaseDatabaseDriver;

  @Bean(name = "budibaseDBDataSource")
  public DataSource budibaseDBDataSource() {
    return DataSourceBuilder.create().url(budibaseDatabaseUrl)
        .username(budibaseDatabaseUsername).password(budibaseDatabasePassword).driverClassName(
            budibaseDatabaseDriver)
        .build();
  }

  @Bean(name = "budibaseDBTemplate")
  public JdbcTemplate budibaseDBTemplate(
      @Qualifier("budibaseDBDataSource") DataSource budibaseDBDataSource) {
    var template = new JdbcTemplate();
    template.setDataSource(budibaseDBDataSource);
    return template;
  }

  @Bean(name = "budibaseDBNamedParameterTemplate")
  public NamedParameterJdbcTemplate budibaseDBNamedParameterTemplate(
      @Qualifier("budibaseDBDataSource") DataSource budibaseDBDataSource) {
    return new NamedParameterJdbcTemplate(budibaseDBDataSource);
  }


}
