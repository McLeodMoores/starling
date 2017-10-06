/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.id.UniqueIdentifiable;
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
  //TODO bad hard-coding
  protected final double _absoluteTolerance = 1e-12;
  protected final double _relativeTolerance = 1e-12;
  protected final int _maxSteps = 100;

  public static DiscountingMethodCurveSetUp setUp() {
    return new DiscountingMethodCurveSetUp();
  }

  DiscountingMethodCurveBuilder(final List<List<String>> curveNames, final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves, final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> newNodes,
      final Map<String, ? extends CurveTypeSetUpInterface> curveGenerators,
      final MulticurveProviderDiscount knownData,
      final CurveBuildingBlockBundle knownBundle) {
    super(curveNames, discountingCurves, iborCurves, overnightCurves, newNodes, curveGenerators, knownData, knownBundle);
    _curveBuildingRepository = new MulticurveDiscountBuildingRepository(_absoluteTolerance, _relativeTolerance, _maxSteps);
  }

  @Override
  Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> buildCurves(final MultiCurveBundle[] curveBundles,
      final MulticurveProviderDiscount knownData, final CurveBuildingBlockBundle knownBundle,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves) {
    final LinkedHashMap<String, Currency> convertedDiscountingCurves = new LinkedHashMap<>();
    for (final Pair<String, UniqueIdentifiable> entry : discountingCurves) {
      if (entry.getValue() instanceof Currency) {
        convertedDiscountingCurves.put(entry.getKey(), (Currency) entry.getValue());
      } else {
        throw new UnsupportedOperationException();
      }
    }
    final LinkedHashMap<String, IborIndex[]> convertedIborCurves = new LinkedHashMap<>();
    for (final Pair<String, List<IborTypeIndex>> entry : iborCurves) {
      final IborIndex[] converted = new IborIndex[entry.getValue().size()];
      int i = 0;
      for (final IborTypeIndex index : entry.getValue()) {
        converted[i++] = IndexConverter.toIborIndex(index);
      }
      convertedIborCurves.put(entry.getKey(), converted);
    }
    final LinkedHashMap<String, IndexON[]> convertedOvernightCurves = new LinkedHashMap<>();
    for (final Map.Entry<String, List<OvernightIndex>> entry : overnightCurves) {
      final IndexON[] converted = new IndexON[entry.getValue().size()];
      int i = 0;
      for (final OvernightIndex index : entry.getValue()) {
        converted[i++] = IndexConverter.toIndexOn(index);
      }
      convertedOvernightCurves.put(entry.getKey(), converted);
    }
    if (knownBundle != null) {
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, convertedDiscountingCurves,
          convertedIborCurves, convertedOvernightCurves, CALCULATOR,
          SENSITIVITY_CALCULATOR);
    }
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, convertedDiscountingCurves,
        convertedIborCurves, convertedOvernightCurves, CALCULATOR,
        SENSITIVITY_CALCULATOR);
  }

}
