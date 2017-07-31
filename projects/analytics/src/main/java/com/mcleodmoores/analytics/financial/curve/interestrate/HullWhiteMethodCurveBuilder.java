/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.CurveUtils;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.hullwhite.HullWhiteProviderDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class HullWhiteMethodCurveBuilder extends CurveBuilder<HullWhiteOneFactorProviderDiscount> {
  private static final ParSpreadMarketQuoteHullWhiteCalculator CALCULATOR =
      ParSpreadMarketQuoteHullWhiteCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator SENSITIVITY_CALCULATOR =
      ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator.getInstance();
  private final HullWhiteProviderDiscountBuildingRepository _curveBuildingRepository;
  //TODO fixing ts, known data should be passed into the build method
  //TODO market data should be passed into the build method - painful now because constructing attributes is annoying
  //TODO bad hard-coding
  protected final double _absoluteTolerance = 1e-12;
  protected final double _relativeTolerance = 1e-12;
  protected final int _maxSteps = 100;

  public static HullWhiteMethodCurveSetUp setUp() {
    return new HullWhiteMethodCurveSetUp();
  }

  HullWhiteMethodCurveBuilder(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves,
      final LinkedHashMap<String, IborIndex[]> iborCurves, final LinkedHashMap<String, IndexON[]> overnightCurves,
      final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes,
      final Map<String, List<InstrumentDefinition<?>>> newNodes,
      final Map<String, ? extends CurveTypeSetUpInterface<HullWhiteOneFactorProviderDiscount>> curveGenerators,
          final HullWhiteOneFactorProviderDiscount knownData, final CurveBuildingBlockBundle knownBundle, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, newNodes, curveGenerators, knownData, knownBundle, fixingTs);
    _curveBuildingRepository = new HullWhiteProviderDiscountBuildingRepository(_absoluteTolerance, _relativeTolerance, _maxSteps);
  }

  @Override
  Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> buildCurves(final MultiCurveBundle[] curveBundles,
      final HullWhiteOneFactorProviderDiscount knownData, final CurveBuildingBlockBundle knownBundle,
      final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves) {
    if (knownBundle != null) {
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, discountingCurves, iborCurves, overnightCurves,
          CALCULATOR, SENSITIVITY_CALCULATOR);
    }
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, discountingCurves, iborCurves, overnightCurves, CALCULATOR,
        SENSITIVITY_CALCULATOR);
  }

  @Override
  CurveBuilder<HullWhiteOneFactorProviderDiscount> replaceMarketQuote(
      final List<String[]> curveNames,
      final LinkedHashMap<String, Currency> discountingCurves,
      final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves,
      final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> newNodesForCurve,
      final Map<String, ? extends CurveTypeSetUpInterface<HullWhiteOneFactorProviderDiscount>> curveGenerators,
          final HullWhiteOneFactorProviderDiscount knownData,
          final CurveBuildingBlockBundle knownBundle,
          final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    return new HullWhiteMethodCurveBuilder(curveNames, discountingCurves, iborCurves, overnightCurves, newNodesForCurve, new HashMap<String, List<InstrumentDefinition<?>>>(),
        curveGenerators, knownData, knownBundle, fixingTs);
  }

  //TODO cache definitions on LocalDate
  public Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> buildCurvesWithoutConvexityAdjustment(final ZonedDateTime valuationDate) {
    final Map<String, GeneratorYDCurve> generatorForCurve = new HashMap<>();
    final int size = getCurveNames().size();
    final MultiCurveBundle[] curveBundles = new MultiCurveBundle[size];
    for (int i = 0; i < size; i++) {
      final String[] curveNamesForUnit = getCurveNames().get(i);
      final SingleCurveBundle[] unitBundle = new SingleCurveBundle[curveNamesForUnit.length];
      for (int j = 0; j < curveNamesForUnit.length; j++) {
        final String curveName = curveNamesForUnit[j];
        final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = getNodes().get(curveName);
        if (nodesForCurve == null) {
          throw new IllegalStateException();
        }
        final Iterator<Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodesIterator = nodesForCurve.entrySet().iterator();
        final int nNodes = nodesForCurve.size();
        final InstrumentDerivative[] instruments = new InstrumentDerivative[nNodes];
        //TODO could do sorting of derivatives here
        final double[] curveInitialGuess = new double[nNodes];
        for (int k = 0; k < nNodes; k++) {
          final Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double> info = nodesIterator.next();
          final InstrumentDefinition<?> definition = info.getKey().getFirst().generateInstrument(valuationDate, info.getValue(), 1, info.getKey().getSecond());
          instruments[k] = CurveUtils.convert(definition, getFixingTs(), valuationDate);
          curveInitialGuess[k] = definition.accept(CurveUtils.RATES_INITIALIZATION);
        }
        final GeneratorYDCurve instrumentGenerator = getCurveGenerators().get(curveName).buildCurveGenerator(valuationDate).finalGenerator(instruments);
        generatorForCurve.put(curveName, instrumentGenerator);
        unitBundle[j] = new SingleCurveBundle<>(curveName, instruments, instrumentGenerator.initialGuess(curveInitialGuess), instrumentGenerator);
      }
      curveBundles[i] = new MultiCurveBundle<>(unitBundle);
    }
    final MulticurveDiscountBuildingRepository curveBuildingRepository =
        new MulticurveDiscountBuildingRepository(_absoluteTolerance, _relativeTolerance, _maxSteps);
    final MulticurveProviderDiscount knownData = getKnownData().getMulticurveProvider();
    if (getKnownBundle() != null) {
      return curveBuildingRepository.makeCurvesFromDerivatives(
          curveBundles, knownData, getKnownBundle(), getDiscountingCurves(), getIborCurves(), getOvernightCurves(),
          ParSpreadMarketQuoteDiscountingCalculator.getInstance(), ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance());
    }
    return curveBuildingRepository.makeCurvesFromDerivatives(
        curveBundles, knownData, getDiscountingCurves(), getIborCurves(), getOvernightCurves(),
        ParSpreadMarketQuoteDiscountingCalculator.getInstance(), ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance());
  }
}
