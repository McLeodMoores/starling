/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 * 
 * @param <RESULT_TYPE> The result-type for the provider.
 */
public class InflationProviderAdapter<RESULT_TYPE> extends InstrumentDerivativeVisitorSameMethodAdapter<InflationProviderInterface, RESULT_TYPE> {
  private final InstrumentDerivativeVisitor<MulticurveProviderInterface, RESULT_TYPE> _visitor;

  public InflationProviderAdapter(final InstrumentDerivativeVisitor<MulticurveProviderInterface, RESULT_TYPE> visitor) {
    _visitor = visitor;
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative) {
    return derivative.accept(_visitor);
  }

  @Override
  public RESULT_TYPE visit(final InstrumentDerivative derivative, final InflationProviderInterface data) {
    return derivative.accept(_visitor, data.getMulticurveProvider());
  }

}
