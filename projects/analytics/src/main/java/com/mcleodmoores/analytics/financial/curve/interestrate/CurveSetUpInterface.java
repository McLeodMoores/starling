/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.Map;

import com.mcleodmoores.analytics.financial.index.Index;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;

/**
 *
 */
public interface CurveSetUpInterface<T extends ParameterProviderInterface> {

  CurveSetUpInterface<T> building(final String... curveNames);

  CurveSetUpInterface<T> buildingFirst(final String... curveNames);

  CurveSetUpInterface<T> thenBuilding(final String... curveNames);

  CurveTypeSetUpInterface<T> using(final String curveName);

  CurveSetUpInterface<T> withNode(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketData);

  //TODO don't need market data here
  CurveSetUpInterface<T> withNode(final String curveName, InstrumentDefinition<?> definition);

  CurveBuilder<T> getBuilder();

  //TODO rename this
  CurveSetUpInterface<T> withKnownData(final T knownData);

  //TODO rename this
  CurveSetUpInterface<T> withKnownBundle(final CurveBuildingBlockBundle knownBundle);

  CurveSetUpInterface<T> withFixingTs(final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs);

  CurveSetUpInterface<T> copy();

}
