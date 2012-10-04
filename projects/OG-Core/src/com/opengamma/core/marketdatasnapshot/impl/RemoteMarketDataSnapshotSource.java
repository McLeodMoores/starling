/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.ObjectChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

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
    
    URI uri = DataMarketDataSnapshotSourceResource.uriGet(getBaseUri(), uniqueId);
    return accessRemote(uri).get(StructuredMarketDataSnapshot.class);
  }

  @Override
  public StructuredMarketDataSnapshot get(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
        
    URI uri = DataMarketDataSnapshotSourceResource.uriGet(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(StructuredMarketDataSnapshot.class);        
  }
  
  @Override
  public void addChangeListener(UniqueId uniqueId, MarketDataSnapshotChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeChangeListener(UniqueId uniqueId, MarketDataSnapshotChangeListener listener) {
    throw new UnsupportedOperationException();
  }

}
