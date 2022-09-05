package com.vi.counselingtoolsservice.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

/**
 * Service for logging.
 */
public class LogService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogService.class);
  public static final String DB_ERROR_TEXT = "Database error: ";
  public static final String FORBIDDEN_WARNING_TEXT = "Forbidden: ";

  private static final String COUNSELING_TOOLS_SERVICE_API = "CounselingToolsService API: {}";

  private LogService() {
  }

  /**
   * Logs a database error.
   *
   * @param exception the exception
   */
  public static void logDatabaseError(Exception exception) {
    LOGGER.error("{}{}", DB_ERROR_TEXT, getStackTrace(exception));
  }

  /**
   * Forbidden warning.
   *
   * @param message the message
   */
  public static void logForbidden(String message) {
    LOGGER.warn("{}{}", FORBIDDEN_WARNING_TEXT, message);
  }

  /**
   * Forbidden warning.
   *
   * @param exception the exception
   */
  public static void logForbidden(Exception exception) {
    LOGGER.warn("{}", getStackTrace(exception));
  }

  /**
   * Log internal server error.
   *
   * @param exception the exception
   */
  public static void logInternalServerError(Exception exception) {
    LOGGER.error(
        "CounselingToolsService Api: {}, {}, {}",
        getStackTrace(exception),
        exception.getMessage(),
        nonNull(exception.getCause()) ? getStackTrace(exception.getCause()) : "No Cause");
  }

  /**
   * Logs an info message.
   *
   * @param msg The message
   */
  public static void logInfo(String msg) {
    LOGGER.info(msg);
  }

  /**
   * Logs an info exception.
   *
   * @param exception the exception
   */
  public static void logInfo(Exception exception) {
    LOGGER.info(getStackTrace(exception));
  }

  /**
   * Logs an warning message.
   *
   * @param exception The exception
   */
  public static void logWarn(Exception exception) {
    LOGGER.warn(getStackTrace(exception));
  }

  /**
   * Logs a warning.
   *
   * @param status the http status to be logged
   * @param ex the exception to be logged
   */
  public static void logWarn(final HttpStatus status, final Exception ex) {
    LOGGER.warn(COUNSELING_TOOLS_SERVICE_API + ": {}", status.getReasonPhrase(), getStackTrace(ex));
  }

}
