/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.CurveSetUpInterface.RootFinderSetUp;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveProviderForwardBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class DirectForwardMethodCurveBuilder extends CurveBuilder<MulticurveProviderForward> {
  private static final ParSpreadMarketQuoteDiscountingCalculator CALCULATOR = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator SENSITIVITY_CALCULATOR = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator
      .getInstance();
  private final MulticurveProviderForwardBuildingRepository _curveBuildingRepository;

  public static DirectForwardMethodCurveSetUp setUp() {
    return new DirectForwardMethodCurveSetUp();
  }

  DirectForwardMethodCurveBuilder(final List<List<String>> curveNames,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface> curveTypes,
      final FXMatrix fxMatrix,
      final Map<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
      final CurveBuildingBlockBundle knownBundle,
      final RootFinderSetUp rootFinder) {
    super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, curveTypes, fxMatrix, preConstructedCurves, knownBundle);
    _curveBuildingRepository = new MulticurveProviderForwardBuildingRepository(rootFinder.getAbsoluteTolerance(), rootFinder.getRelativeTolerance(),
        rootFinder.getMaxSteps(), rootFinder.getRootFinderName());
  }

  @Override
  Pair<MulticurveProviderForward, CurveBuildingBlockBundle> buildCurves(
      final MultiCurveBundle[] curveBundles, final CurveBuildingBlockBundle knownBundle,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final FXMatrix fxMatrix,
      final Map<Currency, YieldAndDiscountCurve> knownDiscountingCurves,
      final Map<IborIndex, YieldAndDiscountCurve> knownIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> knownOvernightCurves) {
    final LinkedHashMap<String, Currency> convertedDiscountingCurves = new LinkedHashMap<>();
    for (final Pair<String, UniqueIdentifiable> entry : discountingCurves) {
      if (entry.getValue() instanceof Currency) {
        convertedDiscountingCurves.put(entry.getKey(), (Currency) entry.getValue());
      } else {
        throw new UnsupportedOperationException();
      }
    }
    final LinkedHashMap<String, IborIndex> singleIborIndices = new LinkedHashMap<>();
    for (final Pair<String, List<IborTypeIndex>> entry : iborCurves) {
      if (entry.getValue().size() != 1) {
        // TODO should be checked when created
        throw new IllegalStateException();
      }
      singleIborIndices.put(entry.getKey(), IndexConverter.toIborIndex(entry.getValue().get(0)));
    }
    final LinkedHashMap<String, IndexON> singleOvernightIndices = new LinkedHashMap<>();
    for (final Pair<String, List<OvernightIndex>> entry : overnightCurves) {
      if (entry.getValue().size() != 1) {
        // TODO should be checked when created
        throw new IllegalStateException();
      }
      singleOvernightIndices.put(entry.getKey(), IndexConverter.toIndexOn(entry.getValue().get(0)));
    }
    final Map<IborIndex, DoublesCurve> temp = new HashMap<>();
    for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : knownIborCurves.entrySet()) {
      temp.put(entry.getKey(), ((YieldCurve) entry.getValue()).getCurve()); // TODO unchecked cast
    }
    final MulticurveProviderForward knownData = new MulticurveProviderForward(knownDiscountingCurves, temp, knownOvernightCurves, fxMatrix);
    if (knownBundle != null) {
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, convertedDiscountingCurves,
          singleIborIndices,
          singleOvernightIndices, CALCULATOR, SENSITIVITY_CALCULATOR);
    }
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, convertedDiscountingCurves, singleIborIndices,
        singleOvernightIndices, CALCULATOR, SENSITIVITY_CALCULATOR);
  }

}
