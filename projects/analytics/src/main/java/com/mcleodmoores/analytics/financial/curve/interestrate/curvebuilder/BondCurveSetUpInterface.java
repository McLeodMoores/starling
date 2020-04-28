/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;

/**
 * A builder interface that defines how to build a set of bond curves using root-finding i.e.
 * <ul>
 * <li>define the order of curve construction with {@link #building}, {@link #buildingFirst} or {@link #thenBuilding}</li>
 * <li>describe what each curve will be used for with {@link #using(String)} or {@link #using(YieldAndDiscountCurve)} e.g. use the curve "SONIA" to discount GBP
 * payments</li>
 * <li>add nodes to each curve</li>
 * </ul>
 *
 * There are optional methods that are used for more specialised curve construction or to define the parameters of the root-finding.
 * <li>add any FX rates needed for cross-currency curve construction</li>
 * <li>add any data from pre-constructed curves</li>
 * <li>set absolute and relative tolerances or the number of steps used for root-finding</li>
 * </ul>
 * Implementing classes should probably use covariant return types. See implementing classes' documentation for example configurations.
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
  CurveBuilder<? extends ParameterIssuerProviderInterface> getBuilder();

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
