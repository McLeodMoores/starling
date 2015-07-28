/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.exposure.factory;

import java.util.Collections;
import java.util.List;

import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
//TODO get rid of repeated code
public class IdSchemeExposureFunction implements NamedExposureFunction {

  /**
   * The name of this exposure function.
   */
  public static final String NAME = "ID Scheme";
  /** The scheme */
  private final ExternalScheme _scheme;

  public IdSchemeExposureFunction(final String schemeName) {
    ArgumentChecker.notNull(schemeName, "schemeName");
    _scheme = ExternalScheme.of(schemeName);
  }

  @Override
  public List<ExternalId> getIds(final Trade trade, final FunctionExecutionContext context) {
    final Security security = trade.getSecurity();
    final ExternalId id = security.getExternalIdBundle().getExternalId(_scheme);
    if (id != null) {
      return Collections.singletonList(id);
    }
    return null;
  }

  @Override
  public List<ExternalId> getIds(final Trade trade, final FunctionCompilationContext context) {
    final Security security = trade.getSecurity();
    final ExternalId id = security.getExternalIdBundle().getExternalId(_scheme);
    if (id != null) {
      return Collections.singletonList(id);
    }
    return null;
  }

  @Override
  public List<ExternalId> getIds(final Trade trade) {
    final Security security = trade.getSecurity();
    final ExternalId id = security.getExternalIdBundle().getExternalId(_scheme);
    if (id != null) {
      return Collections.singletonList(id);
    }
    return null;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
