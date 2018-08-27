/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.net.URI;

import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides remote access to an {@link MarketDataSnapshotSource}.
 */
public class RemoteMarketDataSnapshotSource extends AbstractRemoteSource<StructuredMarketDataSnapshot> implements MarketDataSnapshotSource {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteMarketDataSnapshotSource(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public StructuredMarketDataSnapshot get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    final URI uri = DataMarketDataSnapshotSourceUris.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(StructuredMarketDataSnapshot.class);
  }

  @Override
  public StructuredMarketDataSnapshot get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final URI uri = DataMarketDataSnapshotSourceUris.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(StructuredMarketDataSnapshot.class);
  }

  @Override
  public void addChangeListener(final UniqueId uniqueId, final MarketDataSnapshotChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeChangeListener(final UniqueId uniqueId, final MarketDataSnapshotChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <S extends NamedSnapshot> S getSingle(final Class<S> type,
                                               final String snapshotName,
                                               final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(snapshotName, "snapshotName");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    final URI uri = DataMarketDataSnapshotSourceUris.uriSearchSingle(getBaseUri(), type, snapshotName, versionCorrection);
    final NamedSnapshot snapshot = accessRemote(uri).get(NamedSnapshot.class);

    if (type.isAssignableFrom(snapshot.getClass())) {
      return type.cast(snapshot);
    } else {
      throw new IllegalArgumentException("The requested object is of type: " +
                                             snapshot.getClass().getName() + ", not " + type.getName());
    }
  }
}
