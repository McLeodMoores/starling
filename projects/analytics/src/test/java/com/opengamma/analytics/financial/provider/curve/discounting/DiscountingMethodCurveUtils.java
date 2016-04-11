/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.builder.CurveBuilderSetUp;
import com.opengamma.analytics.financial.provider.curve.builder.CurveTypeSetUpInterface;
import com.opengamma.analytics.financial.provider.curve.builder.DiscountingMethodCurveBuilder;
import com.opengamma.analytics.financial.provider.curve.discounting.DiscountingMethodCurveUtils.DiscountingMethodCurveBuilder2.CurveBuilderSetUp2;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
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

  public static class DiscountingMethodCurveBuilder2 extends DiscountingMethodCurveBuilder {

    DiscountingMethodCurveBuilder2(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
        final LinkedHashMap<String, IndexON[]> overnightCurves, final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes,
        final Map<String, Interpolator1D> interpolatorForCurve, final MulticurveProviderDiscount knownData, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
      super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, interpolatorForCurve, knownData, fixingTs);
    }

    public static CurveBuilderSetUp2 setUp() {
      return new CurveBuilderSetUp2();
    }

    @Override
    public Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate) {
      throw new UnsupportedOperationException();
    }

    public Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate, final ZonedDateTime[] meetingDates) {
      final double[] meetingTimes = new double[meetingDates.length];
      for (int i = 0; i < meetingTimes.length; i++) {
        meetingTimes[i] = TimeCalculator.getTimeBetween(valuationDate, meetingDates[i]);
      }
      MultiCurveBundle<GeneratorYDCurve>[] curveBundles = _cached.get(valuationDate);
      if (curveBundles == null) {
        curveBundles = new MultiCurveBundle[_curveNames.size()];
        for (int i = 0; i < _curveNames.size(); i++) {
          final String[] curveNamesForUnit = _curveNames.get(i);
          final SingleCurveBundle[] unitBundle = new SingleCurveBundle[curveNamesForUnit.length];
          for (int j = 0; j < curveNamesForUnit.length; j++) {
            final String curveName = curveNamesForUnit[j];
            final Interpolator1D interpolator = _interpolatorForCurve.get(curveName);
            final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = _nodes.get(curveName);
            final Iterator<Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodesIterator = nodesForCurve.entrySet().iterator();
            final int nNodes = nodesForCurve.size();
            final InstrumentDerivative[] instruments = new InstrumentDerivative[nNodes];
            //TODO could do sorting of derivatives here
            final double[] curveInitialGuess = new double[nNodes];
            final List<Object> temp = new ArrayList<>();
            for (int k = 0; k < nNodes; k++) {
              final Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double> info = nodesIterator.next();
              final InstrumentDefinition<?> definition = info.getKey().getFirst().generateInstrument(valuationDate, info.getValue(), 1, info.getKey().getSecond());
              temp.add(definition);
              instruments[k] = CurveUtils.convert(definition, _fixingTs, valuationDate);
              curveInitialGuess[k] = definition.accept(CurveUtils.RATES_INITIALIZATION);
            }
            if (curveName.equals("EUR Dsc")) {
              final GeneratorYDCurve instrumentGenerator = new GeneratorCurveDiscountFactorInterpolatedNode(meetingTimes, interpolator);
              unitBundle[j] = new SingleCurveBundle<GeneratorYDCurve>(curveName, instruments, curveInitialGuess, instrumentGenerator.finalGenerator(instruments));
            } else {
              final GeneratorYDCurve instrumentGenerator = new GeneratorCurveYieldInterpolated(LastTimeCalculator.getInstance(), interpolator);
              unitBundle[j] = new SingleCurveBundle<GeneratorYDCurve>(curveName, instruments, curveInitialGuess, instrumentGenerator.finalGenerator(instruments));
            }
          }
          curveBundles[i] = new MultiCurveBundle(unitBundle);
        }
      }
      _cached.put(valuationDate, curveBundles);
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, _knownData, _discountingCurves, _iborCurves, _overnightCurves, CALCULATOR,
          SENSITIVITY_CALCULATOR);
    }

    public static class CurveBuilderSetUp2 extends CurveBuilderSetUp {

      protected CurveBuilderSetUp2() {
        super();
      }

      protected CurveBuilderSetUp2(final CurveBuilderSetUp2 setup) {
        super(setup);
      }

      protected CurveBuilderSetUp2(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
          final LinkedHashMap<String, IndexON[]> overnightCurves, final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes,
          final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final Map<String, Interpolator1D> interpolatorForCurve, final MulticurveProviderDiscount knownData) {
        super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, fixingTs, interpolatorForCurve, knownData);
      }

      @Override
      public CurveBuilderSetUp2 building(final String... curveNames) {
        super.building(curveNames);
        return this;
      }

      @Override
      public CurveBuilderSetUp2 buildingFirst(final String... curveNames) {
        super.buildingFirst(curveNames);
        return this;
      }

      @Override
      public CurveBuilderSetUp2 thenBuilding(final String... curveNames) {
        super.thenBuilding(curveNames);
        return this;
      }

      @Override
      public CurveBuilderSetUp2 withNode(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketData) {
        super.withNode(curveName, instrumentGenerator, attributeGenerator, marketData);
        return this;
      }

      @Override
      public CurveTypeSetUp2 using(final String curveName) {
        return new CurveTypeSetUp2(curveName, this);
      }

      @Override
      public CurveBuilderSetUp2 withKnownData(final MulticurveProviderDiscount knownData) {
        super.withKnownData(knownData);
        return this;
      }

      @Override
      public CurveBuilderSetUp2 withFixingTs(final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
        super.withFixingTs(fixingTs);
        return this;
      }

      @Override
      public DiscountingMethodCurveBuilder2 getBuilder() {
        return new DiscountingMethodCurveBuilder2(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _interpolatorForCurve, _knownData,
            _fixingTs);
      }

      @Override
      public CurveBuilderSetUp2 copy() {
        return new CurveBuilderSetUp2(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _fixingTs, _interpolatorForCurve, _knownData);
      }

    }
  }

  public static class CurveTypeSetUp2 extends CurveBuilderSetUp2 implements CurveTypeSetUpInterface {
    private final String _curveName;

    public CurveTypeSetUp2(final String curveName, final CurveBuilderSetUp2 builder) {
      super(builder);
      _curveName = curveName;
    }

    @Override
    public CurveTypeSetUp2 forDiscounting(final Currency currency) {
      _discountingCurves.put(_curveName, currency);
      return this;
    }

    //TODO versions that only take a single index
    @Override
    public CurveTypeSetUp2 forIborIndex(final IborIndex... indices) {
      _iborCurves.put(_curveName, indices);
      return this;
    }

    @Override
    public CurveTypeSetUp2 forOvernightIndex(final IndexON... indices) {
      _overnightCurves.put(_curveName, indices);
      return this;
    }

    @Override
    public CurveTypeSetUp2 withInterpolator(final Interpolator1D interpolator) {
      _interpolatorForCurve.put(_curveName, interpolator);
      return this;
    }
  }

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

  public static void curveConstructionTest(final InstrumentDefinition<?>[] definitions, final MulticurveProviderDiscount curves,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final FXMatrix fxMatrix, final ZonedDateTime valuationDate,
      final Currency valuationCurrency) {
    for (final InstrumentDefinition<?> definition : definitions) {
      final InstrumentDerivative instrument = CurveUtils.convert(definition, fixingTs, valuationDate);
      final MultipleCurrencyAmount pv = instrument.accept(PVC, curves);
      final double valuationCcyPv = fxMatrix.convert(pv, valuationCurrency).getAmount();
      assertEquals(valuationCcyPv, 0, 1e-9);
    }
  }
}
