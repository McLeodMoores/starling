/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.simpleinstruments.derivative;

/**
 *
 */
public interface SimpleInstrument {

  <S, T> T accept(SimpleInstrumentVisitor<S, T> visitor, S data);

  <S, T> T accept(SimpleInstrumentVisitor<S, T> visitor);
}
