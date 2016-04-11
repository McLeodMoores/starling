/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.builder;

import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;

/**
 *
 */
public interface CurveSetUpInterface {

  public CurveBuilderSetUp building(final String... curveNames);

  public CurveBuilderSetUp buildingFirst(final String... curveNames);

  public CurveBuilderSetUp thenBuilding(final String... curveNames);

  public CurveTypeSetUpInterface using(final String curveName);

  public CurveBuilderSetUp withNode(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketData);

  //TODO add a withNode that takes definitions

  public DiscountingMethodCurveBuilder getBuilder();

  public CurveBuilderSetUp withKnownData(final MulticurveProviderDiscount knownData);

  public CurveBuilderSetUp withFixingTs(final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs);

  public CurveBuilderSetUp copy();
}
