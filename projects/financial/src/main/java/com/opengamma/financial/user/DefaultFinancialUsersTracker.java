/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Tracks userData and clients
 */
public class DefaultFinancialUsersTracker implements FinancialUserDataTracker, FinancialClientTracker {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFinancialUsersTracker.class);

  private final ConcurrentMap<String, Set<String>> _username2clients = new ConcurrentHashMap<>();
  private final ConcurrentMap<ExternalId, Set<ObjectId>> _viewDefinitionIds = new ConcurrentHashMap<>();
  private final ConcurrentMap<ExternalId, Set<ObjectId>> _marketDataSnapShots = new ConcurrentHashMap<>();
  private final FinancialUserServices _services;

  public DefaultFinancialUsersTracker(final FinancialUserServices services) {
    ArgumentChecker.notNull(services, "services");
    _services = services;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the services.
   *
   * @return the services, not null
   */
  public FinancialUserServices getServices() {
    return _services;
  }

  //-------------------------------------------------------------------------
  @Override
  public void created(final String userName, final String clientName, final FinancialUserDataType type, final ObjectId identifier) {
    switch (type) {
      case VIEW_DEFINITION:
        trackCreatedViewDefinition(userName, clientName, identifier);
        break;
      case MARKET_DATA_SNAPSHOT:
        trackCreatedMarketDataSnapshot(userName, clientName, identifier);
        break;
    }
    final Set<String> clients = _username2clients.get(userName);
    if (clients != null) {
      if (clients.contains(clientName)) {
        LOGGER.debug("{} created by {}", identifier, userName);
      }
    } else {
      LOGGER.debug("Late creation of {} by {}", identifier, userName);
    }
  }

  private void trackCreatedMarketDataSnapshot(final String userName, final String clientName, final ObjectId identifier) {
    final ConcurrentSkipListSet<ObjectId> freshIds = new ConcurrentSkipListSet<>();
    Set<ObjectId> marketDataSnapshotIds = _marketDataSnapShots.putIfAbsent(ExternalId.of(userName, clientName), freshIds);
    if (marketDataSnapshotIds == null) {
      marketDataSnapshotIds = freshIds;
    }
    marketDataSnapshotIds.add(identifier);
    LOGGER.debug("{} marketdatasnapshot created by {}", identifier, userName);
  }

  private void trackCreatedViewDefinition(final String userName, final String clientName, final ObjectId identifier) {
    final ConcurrentSkipListSet<ObjectId> freshDefinitions = new ConcurrentSkipListSet<>();
    Set<ObjectId> viewDefinitions = _viewDefinitionIds.putIfAbsent(ExternalId.of(userName, clientName), freshDefinitions);
    if (viewDefinitions == null) {
      viewDefinitions = freshDefinitions;
    }
    viewDefinitions.add(identifier);
    LOGGER.debug("{} view created by {}", identifier, userName);
  }

  @Override
  public void deleted(final String userName, final String clientName, final FinancialUserDataType type, final ObjectId identifier) {
    final Set<String> clients = _username2clients.get(userName);
    if (clients != null) {
      if (clients.contains(clientName)) {
        LOGGER.debug("{} deleted by {}", identifier, userName);
        return;
      }
    }
    LOGGER.debug("Late deletion of {} by {}", identifier, userName);
  }

  @Override
  public void clientCreated(final String userName, final String clientName) {
    final Set<String> clients = _username2clients.get(userName);
    if (clients == null) {
      LOGGER.debug("Late client construction for discarded user {}", userName);
      return;
    }
    clients.add(clientName);
    LOGGER.debug("Client {} created for user {}", clientName, userName);
  }

  @Override
  public void clientDiscarded(final String userName, final String clientName) {
    final Set<String> clients = _username2clients.get(userName);
    if (clients == null) {
      LOGGER.debug("Late client discard for discarded user {}", userName);
    } else {
      clients.remove(clientName);
      LOGGER.debug("Client {} discarded for user {}", clientName, userName);
    }
    removeUserViewDefinitions(userName, clientName);
    removeUserMarketDataSnapshot(userName, clientName);
  }

  private void removeUserMarketDataSnapshot(final String userName, final String clientName) {
    final MarketDataSnapshotMaster marketDataSnapshotMaster = getServices().getSnapshotMaster();
    if (marketDataSnapshotMaster != null) {
      final Set<ObjectId> snapshotIds = _marketDataSnapShots.remove(ExternalId.of(userName, clientName));
      for (final ObjectId oid : snapshotIds) {
        marketDataSnapshotMaster.remove(oid);
        LOGGER.debug("market data snapshot {} discarded for {}/{}", new Object[]{oid, userName, clientName});
      }
    }
  }

  private void removeAllUserMarketDataSnapshot(final String userName) {
    final MarketDataSnapshotMaster marketDataSnapshotMaster = getServices().getSnapshotMaster();
    if (marketDataSnapshotMaster != null) {
      final Iterator<Entry<ExternalId, Set<ObjectId>>> iterator = _marketDataSnapShots.entrySet().iterator();
      while (iterator.hasNext()) {
        final Entry<ExternalId, Set<ObjectId>> entry = iterator.next();
        final ExternalId identifier = entry.getKey();
        if (identifier.getScheme().getName().equals(userName)) {
          final Set<ObjectId> oids = entry.getValue();
          for (final ObjectId oid : oids) {
            marketDataSnapshotMaster.remove(oid);
            LOGGER.debug("market data snapshot {} discarded for {}/{}", new Object[]{oid, userName, identifier.getValue()});
          }
          iterator.remove();
        }
      }
    }
  }

  @Override
  public void userCreated(final String userName) {
    final Set<String> clients = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    _username2clients.putIfAbsent(userName, clients);
    LOGGER.debug("User {} created", userName);
  }

  @Override
  public void userDiscarded(final String userName) {
    final Set<String> removedClients = _username2clients.remove(userName);
    LOGGER.debug("User {} discarded", userName);
    if (removedClients != null) {
      for (final String clientName : removedClients) {
        removeUserViewDefinitions(userName, clientName);
        removeUserMarketDataSnapshot(userName, clientName);
      }
    } else {
      removeAllUserViewDefinitions(userName);
      removeAllUserMarketDataSnapshot(userName);
    }
  }

  private void removeAllUserViewDefinitions(final String userName) {
    final ConfigMaster configMaster = getServices().getConfigMaster();
    if (configMaster != null) {
      final Iterator<Entry<ExternalId, Set<ObjectId>>> iterator = _viewDefinitionIds.entrySet().iterator();
      while (iterator.hasNext()) {
        final Entry<ExternalId, Set<ObjectId>> entry = iterator.next();
        final ExternalId identifier = entry.getKey();
        if (identifier.getScheme().getName().equals(userName)) {
          final Set<ObjectId> viewDefinitions = entry.getValue();
          for (final ObjectId viewDefinitionId : viewDefinitions) {
            configMaster.remove(viewDefinitionId);
            LOGGER.debug("View definition {} discarded for {}/{}", new Object[]{viewDefinitionId, userName, identifier.getValue()});
          }
          iterator.remove();
        }
      }
    }
  }

  private void removeUserViewDefinitions(final String userName, final String clientName) {
    final ConfigMaster configMaster = getServices().getConfigMaster();
    if (configMaster != null) {
      final Set<ObjectId> viewDefinitions = _viewDefinitionIds.remove(ExternalId.of(userName, clientName));
      for (final ObjectId viewDefinitionId : viewDefinitions) {
        configMaster.remove(viewDefinitionId);
        LOGGER.debug("View definition {} discarded for {}/{}", new Object[]{viewDefinitionId, userName, clientName});
      }
    }
  }

}
