package com.vi.counselingtoolsservice.api.controller;


import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@Slf4j
public class BudibaseProxy {

  @RequestMapping(method = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST,
      RequestMethod.DELETE}, value = "/budibaseproxy")
  public void interceptor() throws IOException {
    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest();
    log.info(request.getRequestURI());
    log.info(request.getRemoteAddr());
    log.info(request.getHeaderNames().toString());

  }
}
