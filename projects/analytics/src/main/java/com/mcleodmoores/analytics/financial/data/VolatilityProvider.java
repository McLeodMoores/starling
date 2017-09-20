/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import java.util.Set;

import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public interface VolatilityProvider extends DataProvider {

  @Override
  VolatilityProvider copy();

  Set<UniqueIdentifiable> getIdentifiers();

  Set<UniqueIdentifiable> getIdentifiersForScheme(String scheme);

  //FX
//  double getVolatility(Currency ccy1, Currency ccy2, double time);
//  double getVolatility(final Currency ccy1, final Currency ccy2, final double time, final double strike, final double forward);
//  SmileDeltaParameters getSmile(final Currency ccy1, final Currency ccy2, final double time);

  // bond future option
//  double getVolatility(final double expiry, final double strike);
//  double getVolatility(final double expiry, final double delay, final double strike, final double futuresPrice);

  // cap
//  double getVolatility(final double expiration, final double strike) {

  // STIR option
//  double getVolatility(final double expiry, final double strike, final double delay);

  // swaption
//  double getVolatility(final double expiry, final double maturity) {

}
