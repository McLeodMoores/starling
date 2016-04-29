/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.util.LinkedHashMap;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.builder.CurveSetUpInterface;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class CurveBuildingTestUtils {
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PAR_SPREAD_CALCULATOR =
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PAR_SPREAD_SENSITIVITY_CALCULATOR =
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;
  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY =
      new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);
  private static final double EPS = 1e-9;

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
          derivatives[k] = CurveUtils.convert(definitions[i][j][k], fixingTs, valuationDate);
          initialGuess[k] = definitions[i][j][k].accept(CurveUtils.RATES_INITIALIZATION);
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
      final InstrumentDerivative[][] instruments = CurveUtils.convert(definitions[i], fixingTs, valuationDate);
      for (final InstrumentDerivative[] instrumentsForCurve : instruments) {
        for (final InstrumentDerivative instrument : instrumentsForCurve) {
          final MultipleCurrencyAmount pv = instrument.accept(PVC, curves);
          final double usdPv = fxMatrix.convert(pv, Currency.USD).getAmount();
          assertEquals(usdPv, 0, 1e-9);
        }
      }
    }
  }

  public static <T> void curveConstructionTest(final InstrumentDefinition<?>[] definitions, final T curves,
      final InstrumentDerivativeVisitor<T, MultipleCurrencyAmount> pvCalculator, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs,
      final FXMatrix fxMatrix, final ZonedDateTime valuationDate, final Currency valuationCurrency) {
    curveConstructionTest(definitions, curves, pvCalculator, fixingTs, fxMatrix, valuationDate, valuationCurrency, 1e-9);
  }

  public static <T> void curveConstructionTest(final InstrumentDefinition<?>[] definitions, final T curves,
      final InstrumentDerivativeVisitor<T, MultipleCurrencyAmount> pvCalculator, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs,
      final FXMatrix fxMatrix, final ZonedDateTime valuationDate, final Currency valuationCurrency, final double eps) {
    for (final InstrumentDefinition<?> definition : definitions) {
      final InstrumentDerivative instrument = CurveUtils.convert(definition, fixingTs, valuationDate);
      final MultipleCurrencyAmount pv = instrument.accept(pvCalculator, curves);
      final double valuationCcyPv = fxMatrix.convert(pv, valuationCurrency).getAmount();
      assertEquals(valuationCcyPv, 0, eps);
    }
  }

  public static void assertYieldCurvesEqual(final YieldAndDiscountCurve curve1, final YieldAndDiscountCurve curve2, final double eps) {
    if (curve1 == null) {
      assertNull(curve2);
    }
    assertNotNull(curve2);
    for (double t = 0.001; t < 0.5; t += 0.001) { // below the resolution of a day
      assertEquals(curve1.getDiscountFactor(t), curve2.getDiscountFactor(t), eps);
    }
  }

  public static void assertYieldCurvesNotEqual(final YieldAndDiscountCurve curve1, final YieldAndDiscountCurve curve2, final double eps) {
    if (curve1 == null) {
      assertNotNull(curve2);
    }
    if (curve2 == null) {
      assertNotNull(curve1);
    }
    boolean equals = true;
    for (double t = 0.001; t < 0.5; t += 0.001) { // below the resolution of a day
      if (Math.abs(curve1.getDiscountFactor(t) - curve2.getDiscountFactor(t)) > eps) {
        equals = false;
        break;
      }
    }
    assertFalse(equals);
  }

  public static void assertMatrixEquals(final DoubleMatrix2D matrix1, final DoubleMatrix2D matrix2, final double eps) {
    if (matrix1 == null) {
      assertNull(matrix2);
    }
    assertNotNull(matrix2);
    assertEquals(matrix1.getNumberOfColumns(), matrix2.getNumberOfColumns());
    assertEquals(matrix2.getNumberOfRows(), matrix2.getNumberOfRows());
    for (int i = 0; i < matrix1.getNumberOfRows(); i++) {
      for (int j = 0; j < matrix1.getNumberOfColumns(); j++) {
        assertEquals(matrix1.getEntry(i, j), matrix2.getEntry(i, j), eps);
      }
    }
  }

  public static void assertNoSensitivities(final CurveBuildingBlockBundle fullInverseJacobian,
      final String curveToTest, final String sensitivityCurve) {
    final Pair<CurveBuildingBlock, DoubleMatrix2D> block = fullInverseJacobian.getBlock(curveToTest);
    assertNull(block.getFirst().getData().get(sensitivityCurve));
  }

  public static <T extends ParameterProviderInterface> void assertFiniteDifferenceSensitivities(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final CurveSetUpInterface<T> builder, final String curveToTest,
      final String sensitivityCurve, final ZonedDateTime valuationTime, final GeneratorInstrument<? extends GeneratorAttribute>[] generators,
      final GeneratorAttribute[] attributes, final double[] marketQuotes, final boolean expectZeroSensitivities) {
    final double bump = 1e-6;
    final Pair<CurveBuildingBlock, DoubleMatrix2D> curveToTestBlock = fullInverseJacobian.getBlock(curveToTest);
    final Pair<CurveBuildingBlock, DoubleMatrix2D> sensitivityCurveBlock = fullInverseJacobian.getBlock(sensitivityCurve);
    final int sensitivityCurveSize = sensitivityCurveBlock.getFirst().getNbParameters(sensitivityCurve);
    if (generators.length != sensitivityCurveSize || attributes.length != sensitivityCurveSize || marketQuotes.length != sensitivityCurveSize) {
      fail();
    }
    final DoubleMatrix2D analyticSensitivities = curveToTestBlock.getSecond();
    final int curveToTestSize = curveToTestBlock.getFirst().getNbParameters(curveToTest);
    final int offsetIntoMatrix = curveToTestBlock.getFirst().getStart(sensitivityCurve);
      // check sensitivities against those calculated using finite difference
    for (int i = 0; i < sensitivityCurveSize; i++) {
      final Pair<T, CurveBuildingBlockBundle> upResults = builder.copy()
          .withFixingTs(fixingTs)
          .getBuilder()
          .replaceMarketQuote(sensitivityCurve, generators[i], attributes[i], marketQuotes[i] + bump)
          .buildCurves(valuationTime);
      final Pair<T, CurveBuildingBlockBundle> downResults = builder.copy()
          .withFixingTs(fixingTs)
          .getBuilder()
          .replaceMarketQuote(sensitivityCurve, generators[i], attributes[i], marketQuotes[i] - bump)
          .buildCurves(valuationTime);
      final Double[] upYields = getYData(upResults.getFirst(), curveToTest);
      final Double[] downYields = getYData(downResults.getFirst(), curveToTest);
      final int offset = i + offsetIntoMatrix;
      for (int j = 0; j < curveToTestSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = analyticSensitivities.getData()[j][offset];
        assertEquals(expectedSensitivity, dYielddQuote, bump,
            "Finite difference sensitivities for " + curveToTest + ": column=" + offset + " row=" + j);
        if (expectZeroSensitivities) {
          assertEquals(expectedSensitivity, 0, EPS);
        }
      }
    }
  }

  private static Double[] getYData(final ParameterProviderInterface data, final String curve) {
    if (data instanceof MulticurveProviderDiscount) {
      final YieldAndDiscountCurve yieldOrDiscountCurve = ((MulticurveProviderDiscount) data).getCurve(curve);
      if (yieldOrDiscountCurve instanceof YieldCurve) {
        return ((YieldCurve) yieldOrDiscountCurve).getCurve().getYData();
      } else if (yieldOrDiscountCurve instanceof DiscountCurve) {
        return ((DiscountCurve) yieldOrDiscountCurve).getCurve().getYData();
      }
    } else if (data instanceof HullWhiteOneFactorProviderDiscount) {
      final YieldAndDiscountCurve yieldOrDiscountCurve = ((HullWhiteOneFactorProviderDiscount) data).getMulticurveProvider().getCurve(curve);
      if (yieldOrDiscountCurve instanceof YieldCurve) {
        return ((YieldCurve) yieldOrDiscountCurve).getCurve().getYData();
      } else if (yieldOrDiscountCurve instanceof DiscountCurve) {
        return ((DiscountCurve) yieldOrDiscountCurve).getCurve().getYData();
      }
    } else if (data instanceof MulticurveProviderForward) {
      final Object curveObject = ((MulticurveProviderForward) data).getCurve(curve);
      if (curveObject instanceof YieldCurve) {
        return ((YieldCurve) curveObject).getCurve().getYData();
      } else if (curveObject instanceof DiscountCurve) {
        return ((DiscountCurve) curveObject).getCurve().getYData();
      } else if (curveObject instanceof DoublesCurve) {
        return ((DoublesCurve) curveObject).getYData();
      }
    } else if (data instanceof IssuerProviderDiscount) {
      final YieldAndDiscountCurve yieldOrDiscountCurve = ((IssuerProviderDiscount) data).getCurve(curve);
      if (yieldOrDiscountCurve instanceof YieldCurve) {
        return ((YieldCurve) yieldOrDiscountCurve).getCurve().getYData();
      } else if (yieldOrDiscountCurve instanceof DiscountCurve) {
        return ((DiscountCurve) yieldOrDiscountCurve).getCurve().getYData();
      }
    }
    throw new IllegalStateException();
  }
}
