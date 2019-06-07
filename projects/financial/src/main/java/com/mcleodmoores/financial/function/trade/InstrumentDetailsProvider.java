/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.trade;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;

/**
 *
 */
public interface InstrumentDetailsProvider<INSTRUMENT_TYPE extends InstrumentDefinition<?>, DATA_TYPE extends ParameterProviderInterface> {

  DATA_TYPE getCurves();

  ZonedDateTime getValuationTime();

  INSTRUMENT_TYPE getDefinition();

}
