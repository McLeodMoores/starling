/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.CurveUtils;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.Index;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastFixingStartTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveProviderForwardBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.id.UniqueIdentifiable;
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
  //TODO market data should be passed into the build method - painful now because constructing attributes is annoying
  //TODO bad hard-coding
  protected final double _absoluteTolerance = 1e-10;
  protected final double _relativeTolerance = 1e-10;
  protected final int _maxSteps = 100;

  public static DirectForwardMethodCurveSetUp setUp() {
    return new DirectForwardMethodCurveSetUp();
  }

  DirectForwardMethodCurveBuilder(final List<List<String>> curveNames,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface> curveGenerators,
      final MulticurveProviderForward knownData,
      final CurveBuildingBlockBundle knownBundle) {
    super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, curveGenerators, knownData, knownBundle);
    _curveBuildingRepository = new MulticurveProviderForwardBuildingRepository(_absoluteTolerance, _relativeTolerance, _maxSteps);
    _cached = new HashMap<>();
  }

  @Override
  public Pair<MulticurveProviderForward, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixings) {
    MultiCurveBundle<GeneratorYDCurve>[] curveBundles = _cached.get(valuationDate);
    if (curveBundles == null) {
      final Map<String, GeneratorYDCurve> generatorForCurve = new HashMap<>();
      curveBundles = new MultiCurveBundle[getCurveNames().size()];
      for (int i = 0; i < getCurveNames().size(); i++) {
        final List<String> curveNamesForUnit = getCurveNames().get(i);
        final SingleCurveBundle[] unitBundle = new SingleCurveBundle[curveNamesForUnit.size()];
        for (int j = 0; j < curveNamesForUnit.size(); j++) {
          final String curveName = curveNamesForUnit.get(j);
          final List<InstrumentDefinition<?>> nodesForCurve = getNodes().get(curveName);
          if (nodesForCurve == null) {
            throw new IllegalStateException();
          }
          final int nNodes = nodesForCurve.size();
          final InstrumentDerivative[] instruments = new InstrumentDerivative[nNodes];
          //TODO could do sorting of derivatives here
          final double[] curveInitialGuess = new double[nNodes];
          for (int k = 0; k < nNodes; k++) {
            final InstrumentDefinition<?> definition = nodesForCurve.get(k);
            instruments[k] = CurveUtils.convert(definition, fixings, valuationDate);
            curveInitialGuess[k] = definition.accept(CurveUtils.RATES_INITIALIZATION);
          }
          final CurveTypeSetUpInterface curveTypeSetUpInterface = getCurveGenerators().get(curveName);
          final GeneratorYDCurve instrumentGenerator;
          if (curveTypeSetUpInterface instanceof DirectForwardMethodCurveTypeSetUp) {
            InstrumentDerivativeVisitor<Object, Double> nodeTimeCalculator = LastTimeCalculator.getInstance();
            for (final Pair<String, List<IborTypeIndex>> entry : getIborCurves()) {
              if (entry.getKey().equals(curveName)) {
                nodeTimeCalculator = LastFixingStartTimeCalculator.getInstance();
                break;
              }
            }
            instrumentGenerator =
                ((DirectForwardMethodCurveTypeSetUp) curveTypeSetUpInterface).buildCurveGenerator(valuationDate, nodeTimeCalculator).finalGenerator(instruments);
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
      final CurveBuildingBlockBundle knownBundle, final List<Pair<String, UniqueIdentifiable>> discountingCurves,
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
    if (knownBundle != null) {
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, convertedDiscountingCurves, singleIborIndices,
          singleOvernightIndices, CALCULATOR, SENSITIVITY_CALCULATOR);
    }
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, convertedDiscountingCurves, singleIborIndices, singleOvernightIndices, CALCULATOR,
        SENSITIVITY_CALCULATOR);
  }

}
