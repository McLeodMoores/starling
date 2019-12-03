/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.util.Date;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * An {@code AuditLogger} that sends log messages
 * to a remote destination via Fudge. The messages are consumed by {@link DistributedAuditLoggerServer}.
 */
public class DistributedAuditLogger extends AbstractAuditLogger {

  private static final Logger LOGGER = LoggerFactory.getLogger(DistributedAuditLogger.class);
  private final FudgeMessageSender _msgSender;
  private final FudgeContext _fudgeContext;

  /**
   * Constructs an instance that uses an empty Fudge context and default originating system.
   *
   * @param msgSender  the message sender, not null
   */
  public DistributedAuditLogger(final FudgeMessageSender msgSender) {
    this(getDefaultOriginatingSystem(), msgSender);
  }

  /**
   * Constructs an instance that uses an empty Fudge context.
   *
   * @param originatingSystem  the originating system, not null
   * @param msgSender  the message sender, not null
   */
  public DistributedAuditLogger(final String originatingSystem, final FudgeMessageSender msgSender) {
    this(originatingSystem, msgSender, new FudgeContext());
  }

  /**
   * Constructs an instance.
   *
   * @param originatingSystem  the originating system, not null
   * @param msgSender  the message sender, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public DistributedAuditLogger(final String originatingSystem, final FudgeMessageSender msgSender, final FudgeContext fudgeContext) {
    super(originatingSystem);
    ArgumentChecker.notNull(msgSender, "Message Sender");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");
    _msgSender = msgSender;
    _fudgeContext = fudgeContext;
  }

  @Override
  public void log(final String user, final String originatingSystem, final String object, final String operation,
      final String description, final boolean success) {
    final AuditLogEntry auditLogEntry = new AuditLogEntry(user, originatingSystem, object, operation, description, success, new Date());
    LOGGER.info("Sending message: " + auditLogEntry.toString());
    final FudgeMsg logMessage = auditLogEntry.toFudgeMsg(_fudgeContext);
    _msgSender.send(logMessage);
  }

}
