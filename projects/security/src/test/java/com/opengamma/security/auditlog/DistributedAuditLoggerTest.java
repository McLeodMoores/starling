/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.testng.annotations.Test;

import com.opengamma.transport.ByteArrayFudgeMessageSender;
import com.opengamma.transport.CollectingByteArrayMessageSender;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link DistributedAuditLogger}.
 */
@Test(groups = TestGroup.UNIT)
public class DistributedAuditLoggerTest {
  private static final FudgeContext FUDGE_CONTEXT = new FudgeContext();

  /**
   * Tests the logging.
   */
  public void testClientServerAuditLogging() {
    final CollectingByteArrayMessageSender msgStore = new CollectingByteArrayMessageSender();
    assertEquals(0, msgStore.getMessages().size());

    final DistributedAuditLogger client = new DistributedAuditLogger("testoriginatingsystem", new ByteArrayFudgeMessageSender(msgStore));
    client.log("lisa", "testobject", "testop", "testdescription", true);
    assertEquals(1, msgStore.getMessages().size());

    final FudgeMsgEnvelope fudgeMsgEnvelope = FUDGE_CONTEXT.deserialize(msgStore.getMessages().get(0));

    final InMemoryAuditLogger memoryAuditLogger = new InMemoryAuditLogger();
    assertEquals(0, memoryAuditLogger.getMessages().size());

    final DistributedAuditLoggerServer server = new DistributedAuditLoggerServer(memoryAuditLogger);
    server.messageReceived(FUDGE_CONTEXT, fudgeMsgEnvelope);
    assertEquals(1, memoryAuditLogger.getMessages().size());

    final AuditLogEntry entry = memoryAuditLogger.getMessages().get(0);
    assertEquals("lisa", entry.getUser());
    assertEquals("testoriginatingsystem", entry.getOriginatingSystem());
    assertEquals("testobject", entry.getObject());
    assertEquals("testop", entry.getOperation());
    assertEquals("testdescription", entry.getDescription());
    assertTrue(entry.isSuccess());

    final List<FudgeMsgEnvelope> msgEnvelopes = new ArrayList<>();
    msgEnvelopes.add(fudgeMsgEnvelope);
    msgEnvelopes.add(fudgeMsgEnvelope);
    msgEnvelopes.add(fudgeMsgEnvelope);
    server.messagesReceived(FUDGE_CONTEXT, msgEnvelopes);
    assertEquals(4, memoryAuditLogger.getMessages().size()); // 1 from messageReceived() + 3 from messagesReceived()
  }

}
