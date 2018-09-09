/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.Comparator;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Position;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.CompareUtils;

/**
 * Super-simple comparator for positions or trades that compares the external id bundles, and if the same, compares the quantities of the positions.
 */
public class SimplePositionComparator implements Comparator<Position> {

  @Override
  public int compare(final Position positionOrTrade1, final Position positionOrTrade2) {
    final ExternalIdBundle externalBundle1 = positionOrTrade1.getSecurityLink().getExternalId();
    final ExternalIdBundle externalBundle2 = positionOrTrade2.getSecurityLink().getExternalId();
    final ExternalId bestExId1 = getBestIdentifier(externalBundle1);
    final ExternalId bestExId2 = getBestIdentifier(externalBundle2);
    final int result = CompareUtils.compareWithNullLow(bestExId1, bestExId2);
    if (result == 0) {
      return positionOrTrade2.getQuantity().compareTo(positionOrTrade1.getQuantity());
    }
    return result;
  }

  @SuppressWarnings("deprecation")
  public ExternalId getBestIdentifier(final ExternalIdBundle idBundle) {
    final ExternalScheme[] schemes = {ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.BLOOMBERG_TICKER_WEAK, ExternalSchemes.BLOOMBERG_TCM,
                                ExternalSchemes.ACTIVFEED_TICKER, ExternalSchemes.RIC, ExternalSchemes.ISIN, ExternalSchemes.CUSIP};
    for (final ExternalScheme scheme : schemes) {
      final ExternalId externalId = idBundle.getExternalId(scheme);
      if (externalId != null) {
        return externalId;
      }
    }
    return null;
  }

}
