/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.securities;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.SwapIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.time.Tenor;

/**
 * Creates a list of indices.
 */
public class SimulatedIndexSecuritiesGenerator extends AbstractSecuritiesGenerator {
  /** The indices */
  private static final List<ManageableSecurity> INDICES = new ArrayList<>();

  static {
    final String[] currencies = new String[] { "USD", "EUR", "JPY", "CHF", "GBP", "AUD" };
    final String[] overnightTickers = new String[] { "USDFF", "EONIA", "TONAR", "TOISTOIS", "SONIA", "RBA IBOC" };
    Tenor[] tenors = new Tenor[] { Tenor.ONE_MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS, Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS };
    for (final Tenor tenor : tenors) {
      final String iborTicker = "EUREURIBOR" + tenor.toFormattedString();
      final ExternalId iborIndexId = ExternalSchemes.syntheticSecurityId(iborTicker);
      final IborIndex iborIndex = new IborIndex(iborTicker, tenor, iborIndexId);
      iborIndex.setExternalIdBundle(ExternalIdBundle.of(iborIndexId));
      iborIndex.setName(iborTicker);
      INDICES.add(iborIndex);
    }
    for (int i = 0; i < currencies.length; i++) {
      final String currency = currencies[i];
      final String overnightTicker = overnightTickers[i];
      for (final Tenor tenor : tenors) {
        final String iborTicker = currency + "LIBOR" + tenor.toFormattedString();
        final ExternalId iborIndexId = ExternalSchemes.syntheticSecurityId(iborTicker);
        final IborIndex iborIndex = new IborIndex(iborTicker, tenor, iborIndexId);
        iborIndex.setExternalIdBundle(ExternalIdBundle.of(iborIndexId));
        iborIndex.setName(iborTicker);
        INDICES.add(iborIndex);
      }
      final ExternalId overnightIndexId = ExternalSchemes.syntheticSecurityId(overnightTickers[i]);
      final OvernightIndex overnightIndex = new OvernightIndex(overnightTicker, overnightIndexId);
      overnightIndex.setExternalIdBundle(overnightIndexId.toBundle());
      overnightIndex.setName(overnightTicker);
      INDICES.add(overnightIndex);
    }
    tenors = new Tenor[] { Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS, Tenor.TEN_YEARS };
    for (final Tenor tenor : tenors) {
      final String swapIndexTicker = "USDISDA" + 10 + tenor.toFormattedString().toUpperCase();
      final SwapIndex swapIndex = new SwapIndex(swapIndexTicker, tenor, ExternalSchemes.syntheticSecurityId("USD ISDA Fixing"));
      swapIndex.setExternalIdBundle(ExternalIdBundle.of(ExternalSchemes.syntheticSecurityId(swapIndexTicker)));
      swapIndex.setName(swapIndexTicker);
      INDICES.add(swapIndex);
    }
  }

  @Override
  public SecuritiesGenerator createSecuritiesGenerator() {
    final SecurityGenerator<ManageableSecurity> securityGenerator = new CollectionSecurityGenerator<>(INDICES);
    configure(securityGenerator);
    return new SecuritiesGenerator(securityGenerator, INDICES.size());
  }

}
