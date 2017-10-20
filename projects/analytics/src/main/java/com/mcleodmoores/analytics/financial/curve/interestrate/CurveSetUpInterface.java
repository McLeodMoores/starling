/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;

/**
 *
 */
public interface CurveSetUpInterface {

  CurveSetUpInterface building(String... curveNames);

  CurveSetUpInterface buildingFirst(String... curveNames);

  CurveSetUpInterface thenBuilding(String... curveNames);

  CurveTypeSetUpInterface using(String curveName);

  PreConstructedCurveTypeSetUp using(YieldAndDiscountCurve curve);

  CurveSetUpInterface addNode(String curveName, InstrumentDefinition<?> definition);

  CurveSetUpInterface addFxMatrix(FXMatrix fxMatrix);

  CurveSetUpInterface removeNodes(String curveName);

  CurveBuilder getBuilder();

  CurveSetUpInterface copy();

  CurveSetUpInterface withKnownBundle(CurveBuildingBlockBundle bundle);

  CurveSetUpInterface rootFindingAbsoluteTolerance(double tolerance);

  CurveSetUpInterface rootFindingRelativeTolerance(double tolerance);

  CurveSetUpInterface rootFindingMaximumSteps(int maxSteps);
}
