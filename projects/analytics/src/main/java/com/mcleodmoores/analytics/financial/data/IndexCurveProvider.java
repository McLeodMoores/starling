/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import com.mcleodmoores.analytics.financial.index.Index;

/**
 *
 */
public interface IndexCurveProvider extends IdCurveProvider {

  @Override
  IndexCurveProvider copy();

  double getInvestmentFactor(Index index, double startTime, double endTime, double accrualFactor);

  double getForwardRate(Index index, double startTime, double endTime, double accrualFactor, CompoundingType compoundingType);

  double getForwardRate(Index index, double startTime, double endTime, CompoundingType compoundingType);


}
