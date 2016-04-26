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
public interface CurveSetUpInterface<T extends ParameterProviderInterface> {

  public CurveSetUpInterface<T> building(final String... curveNames);

  public CurveSetUpInterface<T> buildingFirst(final String... curveNames);

  public CurveSetUpInterface<T> thenBuilding(final String... curveNames);

  public CurveTypeSetUpInterface<T> using(final String curveName);

  public CurveSetUpInterface<T> withNode(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketData);

  //TODO add a withNode that takes definitions

  public CurveBuilder<T> getBuilder();

  //TODO rename this
  public CurveSetUpInterface<T> withKnownData(final T knownData);

  //TODO rename this
  public CurveSetUpInterface<T> withKnownBundle(final CurveBuildingBlockBundle knownBundle);

  public CurveSetUpInterface<T> withFixingTs(final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs);

  public CurveSetUpInterface<T> copy();
}
