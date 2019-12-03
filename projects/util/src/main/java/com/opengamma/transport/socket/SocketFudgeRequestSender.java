/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.wire.FudgeMsgReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Opens a raw socket with a remote site for RPC-style communications.
 */
public class SocketFudgeRequestSender extends AbstractSocketProcess implements FudgeRequestSender {
  private static final Logger LOGGER = LoggerFactory.getLogger(SocketFudgeRequestSender.class);
  private final FudgeContext _fudgeContext;

  /**
   * Batch outgoing requests, not to get the benefits of offloading to another thread as we're going
   * to block anyway on a response but to allow concurrent use of the sender to be batched so only
   * one flush operation happens.
   */
  private final MessageBatchingWriter _writer = new MessageBatchingWriter();
  private FudgeMsgReader _msgReader;

  public SocketFudgeRequestSender() {
    this(FudgeContext.GLOBAL_DEFAULT);
  }

  public SocketFudgeRequestSender(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public void sendRequest(final FudgeMsg request, final FudgeMessageReceiver responseReceiver) {
    startIfNecessary();
    LOGGER.debug("Dispatching request with {} fields", request.getNumFields());
    _writer.write(request);
    final FudgeMsgEnvelope response;
    synchronized (_msgReader) {
      response = _msgReader.nextMessageEnvelope();
    }
    if (response != null) {
      LOGGER.debug("Got response with {} fields", response.getMessage().getNumFields());
      responseReceiver.messageReceived(getFudgeContext(), response);
    }
  }

  @Override
  protected void socketOpened(final Socket socket, final BufferedOutputStream os, final BufferedInputStream is) {
    _writer.setFudgeMsgWriter(getFudgeContext(), os);
    _msgReader = getFudgeContext().createMessageReader(is);
  }

  @Override
  protected void socketClosed() {
    super.socketClosed();
    _writer.setFudgeMsgWriter(null);
    _msgReader = null;
  }



}
