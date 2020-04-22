/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;

/**
 *
 */
public interface BondCurveSetUpInterface extends CurveSetUpInterface {

  @Override
  BondCurveSetUpInterface building(String... curveNames);

  @Override
  BondCurveSetUpInterface buildingFirst(String... curveNames);

  @Override
  BondCurveSetUpInterface thenBuilding(String... curveNames);

  @Override
  BondCurveTypeSetUpInterface using(String curveName);

  @Override
  PreConstructedCurveTypeSetUp using(YieldAndDiscountCurve curve);

  @Override
  BondCurveSetUpInterface addNode(String curveName, InstrumentDefinition<?> definition);

  @Override
  BondCurveSetUpInterface addFxMatrix(FXMatrix fxMatrix);

  @Override
  BondCurveSetUpInterface removeNodes(String curveName);

  @Override
  CurveBuilder getBuilder();

  @Override
  BondCurveSetUpInterface copy();

  @Override
  BondCurveSetUpInterface withKnownBundle(CurveBuildingBlockBundle bundle);

  @Override
  BondCurveSetUpInterface rootFindingAbsoluteTolerance(double tolerance);

  @Override
  BondCurveSetUpInterface rootFindingRelativeTolerance(double tolerance);

  @Override
  BondCurveSetUpInterface rootFindingMaximumSteps(int maxSteps);

}
