/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Objects;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.rest.MasterType;

/**
 * {@link ConnectionManager} implementation that creates an instance of {@link ClientConnection} for each
 * client.  It creates {@link Timer} tasks for each connection that closes them and cleans up if they are idle
 * for too long.  This class is thread safe.
 */
public class ConnectionManagerImpl implements ConnectionManager {

  /** Period for the tasks that check whether the client connections have been idle for too long */
  private static final long DEFAULT_TIMEOUT_CHECK_PERIOD = 20000;
  /** By default a client is disconnected if it hasn't been heard from for 60 seconds */
  private static final long DEFAULT_TIMEOUT = 60000;
  // TODO a better way to generate client IDs
  /** Client ID of the next connection */
  private final AtomicLong _clientConnectionId = new AtomicLong();
  /** Provides a connection to the long-polling HTTP connections */
  private final LongPollingConnectionManager _longPollingConnectionManager;
  /** Maximum time a client is allow to be idle before it's disconnected */
  private final long _timeout;
  /** Period for the tasks that check for idle clients */
  private final long _timeoutCheckPeriod;
  /** Connections keyed on client ID */
  private final Map<String, ClientConnection> _connectionsByClientId = new ConcurrentHashMap<>();
  /** Timer for tasks that check for idle clients */
  private final Timer _timer = new Timer();
  /** For listening for changes in entity data */
  private final ChangeManager _changeManager;
  /** For listening for changes to any data in a master */
  private final MasterChangeManager _masterChangeManager;

  public ConnectionManagerImpl(final ChangeManager changeManager,
      final MasterChangeManager masterChangeManager,
      final LongPollingConnectionManager longPollingConnectionManager) {
    this(changeManager,
        masterChangeManager,
        longPollingConnectionManager,
        DEFAULT_TIMEOUT,
        DEFAULT_TIMEOUT_CHECK_PERIOD);
  }

  public ConnectionManagerImpl(final ChangeManager changeManager,
      final MasterChangeManager masterChangeManager,
      final LongPollingConnectionManager longPollingConnectionManager,
      final long timeout,
      final long timeoutCheckPeriod) {
    _changeManager = changeManager;
    _longPollingConnectionManager = longPollingConnectionManager;
    _timeout = timeout;
    _timeoutCheckPeriod = timeoutCheckPeriod;
    _masterChangeManager = masterChangeManager;
  }

  /**
   * Creates a new connection for a client and returns its client ID.  The client ID should be used by the client
   * when subscribing for asynchronous updates.  A connection typically corresponds to a single browser tab or
   * window.  A user can have multiple simultaneous connections.
   * @param userId The ID of the user creating the connection, null if not known
   * @return The client ID of the new connection, must be supplied by the client when subscribing for updates
   */
  @Override
  public String clientConnected(final String userId) {
    final String clientId = Long.toString(_clientConnectionId.getAndIncrement());
    final ConnectionTimeoutTask timeoutTask = new ConnectionTimeoutTask(this, userId, clientId, _timeout);
    final LongPollingUpdateListener updateListener = _longPollingConnectionManager.handshake(userId, clientId, timeoutTask);
    final ClientConnection connection = new ClientConnection(userId, clientId, updateListener, timeoutTask);
    _changeManager.addChangeListener(connection);
    _masterChangeManager.addChangeListener(connection);
    _connectionsByClientId.put(clientId, connection);
    _timer.scheduleAtFixedRate(timeoutTask, _timeoutCheckPeriod, _timeoutCheckPeriod);
    return clientId;
  }

  @Override
  public void clientDisconnected(final String userId, final String clientId) {
    final ClientConnection connection = getConnectionByClientId(userId, clientId);
    _connectionsByClientId.remove(clientId);
    _changeManager.removeChangeListener(connection);
    _masterChangeManager.removeChangeListener(connection);
    _longPollingConnectionManager.disconnect(clientId);
    connection.disconnect();
  }

  @Override
  public void subscribe(final String userId, final String clientId, final UniqueId uid, final String url) {
    getConnectionByClientId(userId, clientId).subscribe(uid, url);
  }

  @Override
  public void subscribe(final String userId, final String clientId, final MasterType masterType, final String url) {
    getConnectionByClientId(userId, clientId).subscribe(masterType, url);
  }

  /**
   * Returns the {@link ClientConnection} corresponding to a client ID.
   * @param userId The ID of the user who owns the connection, null if not known
   * @param clientId The client ID
   * @return The connection
   * @throws DataNotFoundException If there is no connection for the specified ID, the user ID is invalid or if
   * the client and user IDs don't correspond
   */
  // TODO not sure this should be public
  // TODO or should it be specified in ClientConnection?
  @Override
  public ClientConnection getConnectionByClientId(final String userId, final String clientId) {
    ArgumentChecker.notEmpty(clientId, "clientId");
    final ClientConnection connection = _connectionsByClientId.get(clientId);
    if (connection == null) {
      throw new DataNotFoundException("Unknown client ID: " + clientId);
    }
    final String uId = "permissive".equals(userId) ? null : userId;
    if (!Objects.equal(uId, connection.getUserId())) {
      throw new DataNotFoundException("User ID " + uId + " is not associated with client ID " + clientId);
    }
    return connection;
  }

}
