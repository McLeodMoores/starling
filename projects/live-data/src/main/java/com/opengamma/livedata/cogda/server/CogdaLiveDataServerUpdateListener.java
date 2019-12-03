/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.LiveDataValueUpdateBeanFudgeBuilder;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Listens to Fudge updates containing {@link LiveDataValueUpdateBean} instances and
 * dispatches them to a {@link CogdaLiveDataServer}.
 */
public class CogdaLiveDataServerUpdateListener implements FudgeMessageReceiver {
  private final CogdaLiveDataServer _liveDataServer;

  public CogdaLiveDataServerUpdateListener(final CogdaLiveDataServer liveDataServer) {
    ArgumentChecker.notNull(liveDataServer, "liveDataServer");
    _liveDataServer = liveDataServer;
  }

  /**
   * Gets the liveDataServer.
   * @return the liveDataServer
   */
  public CogdaLiveDataServer getLiveDataServer() {
    return _liveDataServer;
  }

  @Override
  public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
    // TODO kirk 2012-08-13 -- Check schema version.
    final FudgeMsg msg = msgEnvelope.getMessage();
    final LiveDataValueUpdateBean updateBean = LiveDataValueUpdateBeanFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(fudgeContext), msg);
    getLiveDataServer().liveDataReceived(updateBean);
  }

}
