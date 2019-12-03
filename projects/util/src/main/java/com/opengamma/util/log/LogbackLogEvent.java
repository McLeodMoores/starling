/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.log;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;

import com.opengamma.util.ArgumentChecker;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Implementation of {@link LogEvent} which handles logs sent through the Logback logging framework.
 */
public class LogbackLogEvent implements LogEvent {

  private final ILoggingEvent _loggingEvent;

  public LogbackLogEvent(final ILoggingEvent loggingEvent) {
    ArgumentChecker.notNull(loggingEvent, "loggingEvent");
    _loggingEvent = loggingEvent;
  }

  //-------------------------------------------------------------------------
  @Override
  public LogLevel getLevel() {
    switch (getLoggingEvent().getLevel().toInt()) {
      case Priority.FATAL_INT:
        return LogLevel.FATAL;
      case Priority.ERROR_INT:
        return LogLevel.ERROR;
      case Priority.WARN_INT:
        return LogLevel.WARN;
      case Priority.INFO_INT:
        return LogLevel.INFO;
      case Priority.DEBUG_INT:
        return LogLevel.DEBUG;
      case Level.TRACE_INT:
        return LogLevel.TRACE;
      default:
        return LogLevel.WARN;
    }
  }

  @Override
  public String getMessage() {
    return getLoggingEvent().getFormattedMessage();
  }

  //-------------------------------------------------------------------------
  private ILoggingEvent getLoggingEvent() {
    return _loggingEvent;
  }

  //-------------------------------------------------------------------------
  @Override
  public int hashCode() {
    final int prime = 31;
    return prime * (_loggingEvent == null ? 0 : _loggingEvent.hashCode());
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof LogbackLogEvent)) {
      return false;
    }
    final LogbackLogEvent other = (LogbackLogEvent) obj;
    return ObjectUtils.equals(_loggingEvent, other._loggingEvent);
  }



}
