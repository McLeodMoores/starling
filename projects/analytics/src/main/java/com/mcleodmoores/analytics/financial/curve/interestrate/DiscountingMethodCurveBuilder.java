/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.Index;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class DiscountingMethodCurveBuilder extends CurveBuilder<MulticurveProviderDiscount> {
  private static final ParSpreadMarketQuoteDiscountingCalculator CALCULATOR =
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator SENSITIVITY_CALCULATOR =
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private final MulticurveDiscountBuildingRepository _curveBuildingRepository;
  //TODO fixing ts, known data should be passed into the build method
  //TODO market data should be passed into the build method - painful now because constructing attributes is annoying
  //TODO bad hard-coding
  protected final double _absoluteTolerance = 1e-12;
  protected final double _relativeTolerance = 1e-12;
  protected final int _maxSteps = 100;

  public static DiscountingMethodCurveSetUp setUp() {
    return new DiscountingMethodCurveSetUp();
  }

  DiscountingMethodCurveBuilder(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves,
      final LinkedHashMap<String, IborTypeIndex[]> iborCurves, final LinkedHashMap<String, OvernightIndex[]> overnightCurves,
      final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes, final Map<String, List<InstrumentDefinition<?>>> newNodes,
      final Map<String, ? extends CurveTypeSetUpInterface<MulticurveProviderDiscount>> curveGenerators,
          final MulticurveProviderDiscount knownData, final CurveBuildingBlockBundle knownBundle, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, newNodes, curveGenerators, knownData, knownBundle, fixingTs);
    _curveBuildingRepository = new MulticurveDiscountBuildingRepository(_absoluteTolerance, _relativeTolerance, _maxSteps);
  }

  @Override
  Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> buildCurves(final MultiCurveBundle[] curveBundles, final MulticurveProviderDiscount knownData,
      final CurveBuildingBlockBundle knownBundle, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborTypeIndex[]> iborCurves,
      final LinkedHashMap<String, OvernightIndex[]> overnightCurves) {
    final LinkedHashMap<String, IborIndex[]> convertedIborCurves = new LinkedHashMap<>();
    for (final Map.Entry<String, IborTypeIndex[]> entry : iborCurves.entrySet()) {
      final IborIndex[] converted = new IborIndex[entry.getValue().length];
      int i = 0;
      for (final IborTypeIndex index : entry.getValue()) {
        converted[i++] = IndexConverter.toIborIndex(index);
      }
      convertedIborCurves.put(entry.getKey(), converted);
    }
    final LinkedHashMap<String, IndexON[]> convertedOvernightCurves = new LinkedHashMap<>();
    for (final Map.Entry<String, OvernightIndex[]> entry : overnightCurves.entrySet()) {
      final IndexON[] converted = new IndexON[entry.getValue().length];
      int i = 0;
      for (final OvernightIndex index : entry.getValue()) {
        converted[i++] = IndexConverter.toIndexOn(index);
      }
      convertedOvernightCurves.put(entry.getKey(), converted);
    }
    if (knownBundle != null) {
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, discountingCurves, convertedIborCurves, convertedOvernightCurves, CALCULATOR,
          SENSITIVITY_CALCULATOR);
    }
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, discountingCurves, convertedIborCurves, convertedOvernightCurves, CALCULATOR,
        SENSITIVITY_CALCULATOR);
  }

  @Override
  CurveBuilder<MulticurveProviderDiscount> replaceMarketQuote(
      final List<String[]> curveNames,
      final LinkedHashMap<String, Currency> discountingCurves,
      final LinkedHashMap<String, IborTypeIndex[]> iborCurves,
      final LinkedHashMap<String, OvernightIndex[]> overnightCurves,
      final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> newNodesForCurve,
      final Map<String, ? extends CurveTypeSetUpInterface<MulticurveProviderDiscount>> curveGenerators,
          final MulticurveProviderDiscount knownData,
          final CurveBuildingBlockBundle knownBundle,
          final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    return new DiscountingMethodCurveBuilder(curveNames, discountingCurves, iborCurves, overnightCurves, newNodesForCurve, new HashMap<String, List<InstrumentDefinition<?>>>(),
        curveGenerators, knownData, knownBundle, fixingTs);
  }

}
