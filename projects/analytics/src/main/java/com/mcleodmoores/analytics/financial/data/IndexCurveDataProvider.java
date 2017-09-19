/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import com.opengamma.analytics.financial.instrument.index.Index;

/**
 *
 */
public interface IndexCurveDataProvider extends CurveDataProvider {

  @Override
  IndexCurveDataProvider copy();

  double getInvestmentFactor(Index index, double startTime, double endTime, double accrualFactor);

  // TODO replace with a compounding type enum
  double getSimplyCompoundedForwardRate(Index index, double startTime, double endTime, double accrualFactor);

  double getSimplyCompoundedForwardRate(Index index, double startTime, double endTime);

  double getAnnuallyCompoundedForwardRate(Index index, double startTime, double endTime, double accrualFactor);

  double getAnnuallyCompoundedForwardRate(Index index, double startTime, double endTime);

}
