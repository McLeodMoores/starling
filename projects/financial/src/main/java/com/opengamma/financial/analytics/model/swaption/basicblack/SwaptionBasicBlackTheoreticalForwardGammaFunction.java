/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.basicblack;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionBlackForwardGammaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * @deprecated Deprecated
 */
@Deprecated
public class SwaptionBasicBlackTheoreticalForwardGammaFunction extends SwaptionBasicBlackFunction {
  private static final SwaptionBlackForwardGammaCalculator CALCULATOR = SwaptionBlackForwardGammaCalculator.getInstance();

  /**
   *
   */
  public SwaptionBasicBlackTheoreticalForwardGammaFunction() {
    super(ValueRequirementNames.FORWARD_GAMMA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double result = swaption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, result / 100000000.0));
  }

}
