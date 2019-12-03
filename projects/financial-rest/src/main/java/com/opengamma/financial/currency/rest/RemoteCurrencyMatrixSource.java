/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import java.net.URI;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractRemoteSource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides remote access to a {@link CurrencyMatrixSource}.
 */
public class RemoteCurrencyMatrixSource extends AbstractRemoteSource<CurrencyMatrix> implements CurrencyMatrixSource {

  private final ChangeManager _changeManager;

  /**
   * Creates an instance.
   *
   * @param baseUri the base target URI for all RESTful web services, not null
   */
  public RemoteCurrencyMatrixSource(final URI baseUri) {
    this(baseUri, new BasicChangeManager());
  }

  public RemoteCurrencyMatrixSource(final URI baseUri, final ChangeManager changeManager) {
    super(baseUri);
    _changeManager = changeManager;
  }

  // CurrencyMatrixSource

  @Override
  public CurrencyMatrix getCurrencyMatrix(final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    try {
      final URI uri = DataCurrencyMatrixSourceUris.uriGetMatrix(getBaseUri(), name, versionCorrection);
      return accessRemote(uri).get(CurrencyMatrix.class);
    } catch (final DataNotFoundException ex) {
      return null;
    }
  }

  @Override
  public CurrencyMatrix get(final UniqueId identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    final URI uri = DataCurrencyMatrixSourceUris.uriGetMatrix(getBaseUri(), identifier);
    return accessRemote(uri).get(CurrencyMatrix.class);
  }

  @Override
  public CurrencyMatrix get(final ObjectId identifier, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(identifier, "identifier");
    final URI uri = DataCurrencyMatrixSourceUris.uriGetMatrix(getBaseUri(), identifier, versionCorrection);
    return accessRemote(uri).get(CurrencyMatrix.class);
  }

  // ChangeProvider

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
