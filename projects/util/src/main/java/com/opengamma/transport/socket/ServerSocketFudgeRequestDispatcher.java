/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.TerminatableJobContainer;

/**
 * Receives Fudge encoded requests from raw sockets and dispatches them
 * to a {@link FudgeRequestReceiver} and returns results over the same socket.
 *
 */
public class ServerSocketFudgeRequestDispatcher extends AbstractServerSocketProcess {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerSocketFudgeRequestDispatcher.class);
  private final FudgeRequestReceiver _underlying;
  private final FudgeContext _fudgeContext;

  private final TerminatableJobContainer _messageReceiveJobs = new TerminatableJobContainer();

  public ServerSocketFudgeRequestDispatcher(final FudgeRequestReceiver underlying, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  public ServerSocketFudgeRequestDispatcher(final FudgeRequestReceiver underlying, final FudgeContext fudgeContext, final ExecutorService executorService) {
    super(executorService);
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the underlying
   */
  public FudgeRequestReceiver getUnderlying() {
    return _underlying;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  protected synchronized void socketOpened(final Socket socket) {
    ArgumentChecker.notNull(socket, "socket");
    LOGGER.info("Opened socket to remote side {}", socket.getRemoteSocketAddress());
    InputStream is;
    OutputStream os;
    try {
      is = socket.getInputStream();
      os = socket.getOutputStream();
    } catch (final IOException e) {
      LOGGER.warn("Unable to open InputStream and OutputStream for socket {}", new Object[] {socket}, e);
      return;
    }

    final RequestDispatchJob job = new RequestDispatchJob(socket, new BufferedInputStream(is), new BufferedOutputStream(os));
    _messageReceiveJobs.addJobAndStartThread(job, "Request Dispatch " + socket.getRemoteSocketAddress());
  }

  @Override
  protected void cleanupPreAccept() {
    _messageReceiveJobs.cleanupTerminatedInstances();
  }

  @Override
  public synchronized void stop() {
    super.stop();
    _messageReceiveJobs.terminateAll();
  }

  private class RequestDispatchJob extends TerminatableJob {
    private final Socket _socket;
    private final FudgeMsgReader _reader;
    private final MessageBatchingWriter _writer;

    // NOTE kirk 2010-05-12 -- Have to pass in the InputStream and OutputStream explicitly so that
    // we can force the IOException catch up above.
    RequestDispatchJob(final Socket socket, final InputStream inputStream, final OutputStream outputStream) {
      ArgumentChecker.notNull(socket, "Socket");
      ArgumentChecker.notNull(inputStream, "inputStream");
      ArgumentChecker.notNull(outputStream, "outputStream");
      _socket = socket;
      _reader = getFudgeContext().createMessageReader(inputStream);
      _writer = new MessageBatchingWriter(getFudgeContext(), outputStream);
    }

    @Override
    protected void runOneCycle() {
      if (_socket.isClosed()) {
        terminate();
        return;
      }

      final FudgeMsgEnvelope envelope;
      try {
        envelope = _reader.nextMessageEnvelope();
      } catch (final Exception e) {
        LOGGER.warn("Unable to read message from underlying stream - terminating connection", e);
        terminate();
        return;
      }

      if (envelope == null) {
        LOGGER.info("Nothing available on the stream. Returning and terminating.");
        terminate();
        return;
      }

      final ExecutorService executorService = getExecutorService();
      if (executorService != null) {
        executorService.execute(new Runnable() {
          @Override
          public void run() {
            dispatch(envelope);
          }
        });
      } else {
        dispatch(envelope);
      }

    }

    private void dispatch(final FudgeMsgEnvelope envelope) {
      FudgeMsg response = null;
      try {
        LOGGER.debug("Received message with {} fields. Dispatching to underlying.", envelope.getMessage().getNumFields());
        response = getUnderlying().requestReceived(new FudgeDeserializer(_fudgeContext), envelope);
      } catch (final Exception e) {
        LOGGER.warn("Unable to dispatch message to underlying receiver", e);
        return;
      }
      if (response != null) {
        try {
          LOGGER.debug("Sending response with {} fields.", response.getNumFields());
          _writer.write(response);
        } catch (final Exception e) {
          LOGGER.warn("Unable to dispatch response to client - terminating connection", e);
          terminate();
        }
      }
    }

    @Override
    public void terminate() {
      if (!_socket.isClosed()) {
        try {
          LOGGER.debug("Closing socket");
          _socket.close();
        } catch (final IOException ex) {
          LOGGER.warn("Couldn't close socket to release blocked I/O", ex.getMessage());
        }
      }
      super.terminate();
    }

  }

}
