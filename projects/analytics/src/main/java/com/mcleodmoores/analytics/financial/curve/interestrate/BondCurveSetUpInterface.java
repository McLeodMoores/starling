/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.Map;

import com.mcleodmoores.analytics.financial.index.Index;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;

/**
 *
 */
public interface BondCurveSetUpInterface<T extends ParameterProviderInterface> extends CurveSetUpInterface<T> {

  @Override
  BondCurveSetUpInterface<T> building(String... curveNames);

  @Override
  BondCurveSetUpInterface<T> buildingFirst(String... curveNames);

  @Override
  BondCurveSetUpInterface<T> thenBuilding(String... curveNames);

  @Override
  CurveTypeSetUpInterface<T> using(String curveName);

  @Override
  BondCurveSetUpInterface<T> withNode(String curveName, GeneratorInstrument instrumentGenerator,
      GeneratorAttribute attributeGenerator, double marketData);

  //TODO add a withNode that takes definitions

  @Override
  CurveBuilder<T> getBuilder();

  //TODO rename this
  @Override
  BondCurveSetUpInterface<T> withKnownData(T knownData);

  //TODO rename this
  @Override
  BondCurveSetUpInterface<T> withKnownBundle(CurveBuildingBlockBundle knownBundle);

  @Override
  BondCurveSetUpInterface<T> withFixingTs(Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs);

  @Override
  BondCurveSetUpInterface<T> copy();
}
