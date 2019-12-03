/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import static org.mockito.Mockito.mock;

import com.opengamma.id.UniqueId;
import com.opengamma.web.analytics.rest.MasterType;

/**
 * Test subscription manager that can have a maximum of one connection.
 */
public class TestConnectionManager implements ConnectionManager {

  private volatile UpdateListener _listener;
  private final LongPollingConnectionManager _longPollingConnectionManager;

  public TestConnectionManager() {
    this(null);
  }

  public TestConnectionManager(final LongPollingConnectionManager longPollingConnectionManager) {
    _longPollingConnectionManager = longPollingConnectionManager;
  }

  @Override
  public String clientConnected(final String userId) {
    final ConnectionTimeoutTask timeoutTask = new ConnectionTimeoutTask(mock(ConnectionManager.class), "user", "client", 60000);
    _listener = _longPollingConnectionManager.handshake(userId, LongPollingTest.CLIENT_ID, timeoutTask);
    return LongPollingTest.CLIENT_ID;
  }

  @Override
  public void clientDisconnected(final String userId, final String clientId) {
    throw new UnsupportedOperationException("closeViewport not used in this test");
  }

  @Override
  public void subscribe(final String userId, final String clientId, final UniqueId uid, final String url) {
    throw new UnsupportedOperationException("subscribe not used in this test");
  }

  @Override
  public void subscribe(final String userId, final String clientId, final MasterType masterType, final String url) {
    throw new UnsupportedOperationException("subscribe not implemented");
  }

  @Override
  public ClientConnection getConnectionByClientId(final String userId, final String clientId) {
    throw new UnsupportedOperationException("getConnectionByClientId not implemented");
  }

  public void sendUpdate(final String update) {
    _listener.itemUpdated(update);
  }
}
