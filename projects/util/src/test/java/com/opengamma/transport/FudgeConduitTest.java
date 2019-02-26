/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the various Fudge-specific conduit forms.
 */
@Test(groups = TestGroup.INTEGRATION)
public class FudgeConduitTest {

  /**
   *
   */
  public void oneWayTest() {
    final FudgeContext context = new FudgeContext();
    final CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    final ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(collectingReceiver);
    final DirectInvocationByteArrayMessageSender byteArraySender = new DirectInvocationByteArrayMessageSender(fudgeReceiver);
    final ByteArrayFudgeMessageSender fudgeSender = new ByteArrayFudgeMessageSender(byteArraySender, context);

    final MutableFudgeMsg msg = context.newMessage();
    msg.add("Foo", "Bar");
    msg.add("Number Problems", 99);

    fudgeSender.send(msg);

    final List<FudgeMsgEnvelope> receivedMessages = collectingReceiver.getMessages();
    assertEquals(1, receivedMessages.size());
    final FudgeMsgEnvelope receivedEnvelope = receivedMessages.get(0);
    assertNotNull(receivedEnvelope.getMessage());
    final FudgeMsg receivedMsg = receivedEnvelope.getMessage();
    assertEquals(2, receivedMsg.getNumFields());
    assertEquals("Bar", receivedMsg.getString("Foo"));
    assertEquals(new Integer(99), receivedMsg.getInt("Number Problems"));
  }

  /**
   *
   */
  public void oneWayTestWithEncryption() {
    final FudgeContext context = new FudgeContext();
    final CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    final ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(collectingReceiver, OpenGammaFudgeContext.getInstance(), true);
    final DirectInvocationByteArrayMessageSender byteArraySender = new DirectInvocationByteArrayMessageSender(fudgeReceiver);
    final ByteArrayFudgeMessageSender fudgeSender = new ByteArrayFudgeMessageSender(byteArraySender, context, true);

    final MutableFudgeMsg msg = context.newMessage();
    msg.add("Foo", "Bar");
    msg.add("Number Problems", 99);

    fudgeSender.send(msg);

    final List<FudgeMsgEnvelope> receivedMessages = collectingReceiver.getMessages();
    assertEquals(1, receivedMessages.size());
    final FudgeMsgEnvelope receivedEnvelope = receivedMessages.get(0);
    assertNotNull(receivedEnvelope.getMessage());
    final FudgeMsg receivedMsg = receivedEnvelope.getMessage();
    assertEquals(2, receivedMsg.getNumFields());
    assertEquals("Bar", receivedMsg.getString("Foo"));
    assertEquals(new Integer(99), receivedMsg.getInt("Number Problems"));
  }

  /**
   *
   */
  public void requestResponseTest() {
    final FudgeContext context = new FudgeContext();
    final FudgeRequestReceiver requestReceiver = new FudgeRequestReceiver() {
      @Override
      public FudgeMsg requestReceived(final FudgeDeserializer deserializer, final FudgeMsgEnvelope requestEnvelope) {
        final MutableFudgeMsg response = deserializer.getFudgeContext().newMessage();
        response.add("Killing", "In The Name Of");
        return response;
      }
    };

    final FudgeRequestSender sender = InMemoryRequestConduit.create(requestReceiver);

    final MutableFudgeMsg request = context.newMessage();
    request.add("Rage", "Against The Machine");

    final CollectingFudgeMessageReceiver responseReceiver = new CollectingFudgeMessageReceiver();
    sender.sendRequest(request, responseReceiver);
    final List<FudgeMsgEnvelope> receivedMessages = responseReceiver.getMessages();
    assertEquals(1, receivedMessages.size());

    final FudgeMsgEnvelope receivedEnvelope = receivedMessages.get(0);
    assertNotNull(receivedEnvelope.getMessage());
    final FudgeMsg receivedMsg = receivedEnvelope.getMessage();
    assertEquals(1, receivedMsg.getNumFields());
    assertEquals("In The Name Of", receivedMsg.getString("Killing"));
  }
}
