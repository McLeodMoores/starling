/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.builder;

import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;

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
  public BondCurveSetUpInterface<T> withNode(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketData);

  //TODO add a withNode that takes definitions

  @Override
  public CurveBuilder<T> getBuilder();

  //TODO rename this
  @Override
  public BondCurveSetUpInterface<T> withKnownData(final T knownData);

  //TODO rename this
  @Override
  public BondCurveSetUpInterface<T> withKnownBundle(final CurveBuildingBlockBundle knownBundle);

  @Override
  public BondCurveSetUpInterface<T> withFixingTs(final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs);

  @Override
  public BondCurveSetUpInterface<T> copy();
}
