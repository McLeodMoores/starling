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

  CurveSetUpInterface<T> building(String... curveNames);

  CurveSetUpInterface<T> buildingFirst(String... curveNames);

  CurveSetUpInterface<T> thenBuilding(String... curveNames);

  CurveTypeSetUpInterface<T> using(String curveName);

  CurveSetUpInterface<T> withNode(String curveName, GeneratorInstrument instrumentGenerator, GeneratorAttribute attributeGenerator,
      double marketData);

  //TODO don't need market data here
  CurveSetUpInterface<T> withNode(String curveName, InstrumentDefinition<?> definition);

  CurveBuilder<T> getBuilder();

  //TODO rename this
  CurveSetUpInterface<T> withKnownData(T knownData);

  //TODO rename this
  CurveSetUpInterface<T> withKnownBundle(CurveBuildingBlockBundle knownBundle);

  CurveSetUpInterface<T> withFixingTs(Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs);

  CurveSetUpInterface<T> copy();

}
