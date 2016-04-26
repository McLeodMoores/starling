/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.builder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

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
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveProviderForwardBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class DirectForwardMethodCurveBuilder extends CurveBuilder<MulticurveProviderForward> {
  private static final ParSpreadMarketQuoteDiscountingCalculator CALCULATOR =
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator SENSITIVITY_CALCULATOR =
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private final MulticurveProviderForwardBuildingRepository _curveBuildingRepository;
  private final Map<ZonedDateTime, MultiCurveBundle[]> _cached;
  //TODO fixing ts, known data should be passed into the build method
  //TODO market data should be passed into the build method - painful now because constructing attributes is annoying
  //TODO bad hard-coding
  protected final double _absoluteTolerance = 1e-10;
  protected final double _relativeTolerance = 1e-10;
  protected final int _maxSteps = 100;

  public static DirectForwardMethodCurveSetUp setUp() {
    return new DirectForwardMethodCurveSetUp();
  }

  DirectForwardMethodCurveBuilder(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves,
      final LinkedHashMap<String, IborIndex[]> iborCurves, final LinkedHashMap<String, IndexON[]> overnightCurves,
      final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes, final Map<String, ? extends CurveTypeSetUpInterface<MulticurveProviderForward>> curveGenerators,
      final MulticurveProviderForward knownData, final CurveBuildingBlockBundle knownBundle, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, curveGenerators, knownData, knownBundle, fixingTs);
    _curveBuildingRepository = new MulticurveProviderForwardBuildingRepository(_absoluteTolerance, _relativeTolerance, _maxSteps);
    _cached = new HashMap<>();
  }

  @Override
  public Pair<MulticurveProviderForward, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate) {
    MultiCurveBundle<GeneratorYDCurve>[] curveBundles = _cached.get(valuationDate);
    if (curveBundles == null) {
      final Map<String, GeneratorYDCurve> generatorForCurve = new HashMap<>();
      curveBundles = new MultiCurveBundle[getCurveNames().size()];
      for (int i = 0; i < getCurveNames().size(); i++) {
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
          final CurveTypeSetUpInterface<MulticurveProviderForward> curveTypeSetUpInterface = getCurveGenerators().get(curveName);
          final GeneratorYDCurve instrumentGenerator;
          if (curveTypeSetUpInterface instanceof DirectForwardMethodCurveTypeSetUp) {
            instrumentGenerator = ((DirectForwardMethodCurveTypeSetUp) curveTypeSetUpInterface).buildCurveGenerator(valuationDate, curveName).finalGenerator(instruments);
          } else {
            instrumentGenerator = curveTypeSetUpInterface.buildCurveGenerator(valuationDate).finalGenerator(instruments);
          }
          generatorForCurve.put(curveName, instrumentGenerator);
          unitBundle[j] = new SingleCurveBundle<>(curveName, instruments, instrumentGenerator.initialGuess(curveInitialGuess), instrumentGenerator);
        }
        curveBundles[i] = new MultiCurveBundle<>(unitBundle);
      }
      _cached.put(valuationDate, curveBundles);
    }
    return buildCurves(curveBundles, getKnownData(), getKnownBundle(), getDiscountingCurves(), getIborCurves(), getOvernightCurves());
  }

  @Override
  Pair<MulticurveProviderForward, CurveBuildingBlockBundle> buildCurves(final MultiCurveBundle[] curveBundles, final MulticurveProviderForward knownData,
      final CurveBuildingBlockBundle knownBundle, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves) {
    final LinkedHashMap<String, IborIndex> singleIborIndices = new LinkedHashMap<>();
    for (final Map.Entry<String, IborIndex[]> entry : iborCurves.entrySet()) {
      if (entry.getValue().length != 1) {
        // TODO should be checked when created
        throw new IllegalStateException();
      }
      singleIborIndices.put(entry.getKey(), entry.getValue()[0]);
    }
    final LinkedHashMap<String, IndexON> singleOvernightIndices = new LinkedHashMap<>();
    for (final Map.Entry<String, IndexON[]> entry : overnightCurves.entrySet()) {
      if (entry.getValue().length != 1) {
        // TODO should be checked when created
        throw new IllegalStateException();
      }
      singleOvernightIndices.put(entry.getKey(), entry.getValue()[0]);
    }
    if (knownBundle != null) {
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, discountingCurves, singleIborIndices,
          singleOvernightIndices, CALCULATOR, SENSITIVITY_CALCULATOR);
    }
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, discountingCurves, singleIborIndices, singleOvernightIndices, CALCULATOR,
        SENSITIVITY_CALCULATOR);
  }

  @Override
  CurveBuilder<MulticurveProviderForward> replaceMarketQuote(
      final List<String[]> curveNames,
      final LinkedHashMap<String, Currency> discountingCurves,
      final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves,
      final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> newNodesForCurve,
      final Map<String, ? extends CurveTypeSetUpInterface<MulticurveProviderForward>> curveGenerators,
      final MulticurveProviderForward knownData,
      final CurveBuildingBlockBundle knownBundle,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    return new DirectForwardMethodCurveBuilder(curveNames, discountingCurves, iborCurves, overnightCurves, newNodesForCurve,
        curveGenerators, knownData, knownBundle, fixingTs);
  }

}
