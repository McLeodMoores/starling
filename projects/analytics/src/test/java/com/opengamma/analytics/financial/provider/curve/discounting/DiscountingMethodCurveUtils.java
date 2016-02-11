/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static org.testng.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveTestUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class DiscountingMethodCurveUtils {
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PAR_SPREAD_CALCULATOR =
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PAR_SPREAD_SENSITIVITY_CALCULATOR =
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;
  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY =
      new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);


  @SuppressWarnings("unchecked")
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(
      final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators, final String[][] curveNames,
      final MulticurveProviderDiscount knownData, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final ZonedDateTime valuationDate,
      final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] initialGuess = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = CurveTestUtils.convert(definitions[i][j][k], fixingTs, valuationDate);
          initialGuess[k] = definitions[i][j][k].accept(CurveTestUtils.RATES_INITIALIZATION);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, discountingCurves, iborCurves, overnightCurves, PAR_SPREAD_CALCULATOR,
        PAR_SPREAD_SENSITIVITY_CALCULATOR);
  }

  @SuppressWarnings("unchecked")
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(
      final MultiCurveBundle[] curveBundles, final MulticurveProviderDiscount knownData, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs,
      final ZonedDateTime valuationDate, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves) {
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, discountingCurves, iborCurves, overnightCurves, PAR_SPREAD_CALCULATOR,
        PAR_SPREAD_SENSITIVITY_CALCULATOR);
  }

  public static void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderDiscount curves,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final FXMatrix fxMatrix, final ZonedDateTime valuationDate) {
    final int nbBlocks = definitions.length;
    for (int i = 0; i < nbBlocks; i++) {
      final InstrumentDerivative[][] instruments = CurveTestUtils.convert(definitions[i], fixingTs, valuationDate);
      for (final InstrumentDerivative[] instrumentsForCurve : instruments) {
        for (final InstrumentDerivative instrument : instrumentsForCurve) {
          final MultipleCurrencyAmount pv = instrument.accept(PVC, curves);
          final double usdPv = fxMatrix.convert(pv, Currency.USD).getAmount();
          assertEquals(usdPv, 0, 1e-9);
        }
      }
    }
  }

  public static void curveConstructionTest(final InstrumentDefinition<?>[] definitions, final MulticurveProviderDiscount curves,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final FXMatrix fxMatrix, final ZonedDateTime valuationDate) {
    for (final InstrumentDefinition<?> definition : definitions) {
      final InstrumentDerivative instrument = CurveTestUtils.convert(definition, fixingTs, valuationDate);
      final MultipleCurrencyAmount pv = instrument.accept(PVC, curves);
      final double usdPv = fxMatrix.convert(pv, Currency.USD).getAmount();
      assertEquals(usdPv, 0, 1e-9);
    }
  }
}
