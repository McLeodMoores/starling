/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.pool;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.TransferQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.ConnectionPartition;
import com.jolbox.bonecp.StatementHandle;
import com.jolbox.bonecp.hooks.AcquireFailConfig;
import com.jolbox.bonecp.hooks.ConnectionHook;
import com.jolbox.bonecp.hooks.ConnectionState;
import com.opengamma.util.async.BlockingOperation;

/**
 * Hacks a call to {@link BlockingOperation#wouldBlock} into {@link BoneCP} when it would wait for a connection to become available.
 */
public class BoneCPHack implements ConnectionHook {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BoneCPHack.class);

  private static final Object HACK_PARTITION_FLAG = new Object();

  /**
   * The underlying connection.
   */
  private final ConnectionHook _underlying;

  /**
   * Creates an instance.
   */
  public BoneCPHack() {
    this(null);
  }

  /**
   * Creates an instance decorating the underlying hook.
   *
   * @param underlying  the underlying hook
   */
  public BoneCPHack(final ConnectionHook underlying) {
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public void onAcquire(final ConnectionHandle connection) {
    if (_underlying != null) {
      _underlying.onAcquire(connection);
    }
    if (connection.getDebugHandle() == null) {
      connection.setDebugHandle(HACK_PARTITION_FLAG);
    }
  }

  @Override
  public void onCheckIn(final ConnectionHandle connection) {
    if (_underlying != null) {
      _underlying.onCheckIn(connection);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onCheckOut(final ConnectionHandle connection) {
    if (connection.getDebugHandle() == HACK_PARTITION_FLAG) {
      try {
        final ConnectionPartition partition = connection.getOriginatingPartition();
        final Method getFreeConnections = partition.getClass().getDeclaredMethod("getFreeConnections");
        getFreeConnections.setAccessible(true);
        TransferQueue<ConnectionHandle> connections = (TransferQueue<ConnectionHandle>) getFreeConnections.invoke(partition);
        if (!(connections instanceof TransferQueueWithBlockingOperationHook)) {
          synchronized (partition) {
            connections = (TransferQueue<ConnectionHandle>) getFreeConnections.invoke(partition);
            if (!(connections instanceof TransferQueueWithBlockingOperationHook)) {
              final Method setFreeConnections = partition.getClass().getDeclaredMethod("setFreeConnections", TransferQueue.class);
              setFreeConnections.setAccessible(true);
              setFreeConnections.invoke(partition, new TransferQueueWithBlockingOperationHook<>(connections));
            }
          }
        }
      } catch (final Exception e) {
        LOGGER.error("Couldn't hack BlockingOperation call into BoneCP", e);
      }
      connection.setDebugHandle(null);
    }
    if (_underlying != null) {
      _underlying.onCheckOut(connection);
    }
  }

  @Override
  public void onDestroy(final ConnectionHandle connection) {
    if (connection.getDebugHandle() == HACK_PARTITION_FLAG) {
      connection.setDebugHandle(null);
    }
    if (_underlying != null) {
      _underlying.onDestroy(connection);
    }
  }

  @Override
  public boolean onAcquireFail(final Throwable t, final AcquireFailConfig acquireConfig) {
    if (_underlying != null) {
      return _underlying.onAcquireFail(t, acquireConfig);
    }
    return false;
  }

  @Override
  public void onQueryExecuteTimeLimitExceeded(final ConnectionHandle conn, final Statement statement,
      final String sql, final Map<Object, Object> logParams, final long timeElapsedInNs) {
    if (_underlying != null) {
      _underlying.onQueryExecuteTimeLimitExceeded(conn, statement, sql, logParams, timeElapsedInNs);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onQueryExecuteTimeLimitExceeded(final ConnectionHandle conn, final Statement statement, final String sql, final Map<Object, Object> logParams) {
    if (_underlying != null) {
      _underlying.onQueryExecuteTimeLimitExceeded(conn, statement, sql, logParams);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onQueryExecuteTimeLimitExceeded(final String sql, final Map<Object, Object> logParams) {
    if (_underlying != null) {
      _underlying.onQueryExecuteTimeLimitExceeded(sql, logParams);
    }
  }

  @Override
  public void onBeforeStatementExecute(final ConnectionHandle conn, final StatementHandle statement, final String sql, final Map<Object, Object> params) {
    if (_underlying != null) {
      _underlying.onBeforeStatementExecute(conn, statement, sql, params);
    }
  }

  @Override
  public void onAfterStatementExecute(final ConnectionHandle conn, final StatementHandle statement, final String sql, final Map<Object, Object> params) {
    if (_underlying != null) {
      _underlying.onAfterStatementExecute(conn, statement, sql, params);
    }
  }

  @Override
  public boolean onConnectionException(final ConnectionHandle connection, final String state, final Throwable t) {
    if (_underlying != null) {
      return _underlying.onConnectionException(connection, state, t);
    }
    return false;
  }

  @Override
  public ConnectionState onMarkPossiblyBroken(final ConnectionHandle connection, final String state, final SQLException e) {
    if (_underlying != null) {
      return _underlying.onMarkPossiblyBroken(connection, state, e);
    }
    return ConnectionState.NOP;
  }
}
