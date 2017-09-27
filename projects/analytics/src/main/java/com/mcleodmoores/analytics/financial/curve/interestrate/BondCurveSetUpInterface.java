/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;

/**
 *
 */
public interface BondCurveSetUpInterface<T extends ParameterProviderInterface> extends CurveSetUpInterface<T> {

  @Override
  public BondCurveSetUpInterface<T> building(final String... curveNames);

  @Override
  public BondCurveSetUpInterface<T> buildingFirst(final String... curveNames);

  @Override
  public BondCurveSetUpInterface<T> thenBuilding(final String... curveNames);

  @Override
  public CurveTypeSetUpInterface<T> using(final String curveName);

  @Override
  public CurveBuilder<T> getBuilder();

  //TODO rename this
  @Override
  public BondCurveSetUpInterface<T> withKnownData(final T knownData);

  //TODO rename this
  @Override
  public BondCurveSetUpInterface<T> withKnownBundle(final CurveBuildingBlockBundle knownBundle);

  @Override
  public BondCurveSetUpInterface<T> copy();
}
