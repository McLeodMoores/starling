/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;

/**
 *
 */
public interface CurveSetUpInterface<T extends ParameterProviderInterface> {

  CurveSetUpInterface<T> building(final String... curveNames);

  CurveSetUpInterface<T> buildingFirst(final String... curveNames);

  CurveSetUpInterface<T> thenBuilding(final String... curveNames);

  CurveTypeSetUpInterface using(final String curveName);

  CurveTypeSetUpInterface using(YieldAndDiscountCurve curve);

  CurveSetUpInterface<T> addNode(final String curveName, InstrumentDefinition<?> definition);

  CurveSetUpInterface<T> addFxMatrix(FXMatrix fxMatrix);

  CurveSetUpInterface<T> removeNodes(String curveName);

  CurveBuilder<T> getBuilder();

  //TODO rename this
  CurveSetUpInterface<T> withKnownData(final T knownData);

  //TODO rename this
  CurveSetUpInterface<T> withKnownBundle(final CurveBuildingBlockBundle knownBundle);

  CurveSetUpInterface<T> copy();

}
