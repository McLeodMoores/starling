/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.ArrayList;
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
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public abstract class CurveBuilder<T extends ParameterProviderInterface> {
  private final List<String[]> _curveNames;
  private final LinkedHashMap<String, Currency> _discountingCurves;
  private final LinkedHashMap<String, IborIndex[]> _iborCurves;
  private final LinkedHashMap<String, IndexON[]> _overnightCurves;
  private final Map<String, ? extends CurveTypeSetUpInterface<T>> _curveGenerators;
  private final CurveBuildingBlockBundle _knownBundle;
  private final Map<Index, ZonedDateTimeDoubleTimeSeries> _fixingTs;
  private final T _knownData;
  private final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> _nodes;
  private final Map<ZonedDateTime, MultiCurveBundle[]> _cached;

  CurveBuilder(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves, final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface<T>> curveGenerators, final T knownData, final CurveBuildingBlockBundle knownBundle,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    _curveNames = new ArrayList<>(curveNames);
    _discountingCurves = new LinkedHashMap<>(discountingCurves);
    _iborCurves = new LinkedHashMap<>(iborCurves);
    _overnightCurves = new LinkedHashMap<>(overnightCurves);
    _nodes = new HashMap<>(nodes);
    _curveGenerators = new HashMap<>(curveGenerators);
    _knownData = knownData == null ? null : (T) knownData.copy();
    _knownBundle = knownBundle; // TODO copy this
    _fixingTs = new HashMap<>(fixingTs);
    _cached = new HashMap<>();
  }

  //TODO cache definitions on LocalDate
  public Pair<T, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate) {
    MultiCurveBundle<GeneratorYDCurve>[] curveBundles = _cached.get(valuationDate);
    if (curveBundles == null) {
      final Map<String, GeneratorYDCurve> generatorForCurve = new HashMap<>();
      curveBundles = new MultiCurveBundle[_curveNames.size()];
      for (int i = 0; i < _curveNames.size(); i++) {
        final String[] curveNamesForUnit = _curveNames.get(i);
        final SingleCurveBundle[] unitBundle = new SingleCurveBundle[curveNamesForUnit.length];
        for (int j = 0; j < curveNamesForUnit.length; j++) {
          final String curveName = curveNamesForUnit[j];
          final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = _nodes.get(curveName);
          if (nodesForCurve == null) {
            throw new IllegalStateException("No nodes found for curve called " + curveName);
          }
          final Iterator<Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodesIterator = nodesForCurve.entrySet().iterator();
          final int nNodes = nodesForCurve.size();
          final InstrumentDerivative[] instruments = new InstrumentDerivative[nNodes];
          //TODO could do sorting of derivatives here
          final double[] curveInitialGuess = new double[nNodes];
          for (int k = 0; k < nNodes; k++) {
            final Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double> info = nodesIterator.next();
            final InstrumentDefinition<?> definition = info.getKey().getFirst().generateInstrument(valuationDate, info.getValue(), 1, info.getKey().getSecond());
            instruments[k] = CurveUtils.convert(definition, _fixingTs, valuationDate);
            curveInitialGuess[k] = definition.accept(CurveUtils.RATES_INITIALIZATION);
          }
          final GeneratorYDCurve instrumentGenerator = _curveGenerators.get(curveName).buildCurveGenerator(valuationDate).finalGenerator(instruments);
          generatorForCurve.put(curveName, instrumentGenerator);
          unitBundle[j] = new SingleCurveBundle<>(curveName, instruments, instrumentGenerator.initialGuess(curveInitialGuess), instrumentGenerator);
        }
        curveBundles[i] = new MultiCurveBundle<>(unitBundle);
      }
      _cached.put(valuationDate, curveBundles);
    }
    return buildCurves(curveBundles, _knownData, _knownBundle, _discountingCurves, _iborCurves, _overnightCurves);
  }

  abstract Pair<T, CurveBuildingBlockBundle> buildCurves(MultiCurveBundle[] curveBundles, T knownData, CurveBuildingBlockBundle knownBundle,
      LinkedHashMap<String, Currency> discountingCurves, LinkedHashMap<String, IborIndex[]> iborCurves, LinkedHashMap<String, IndexON[]> overnightCurves);

  public Map<String, InstrumentDefinition<?>[]> getDefinitionsForCurves(final ZonedDateTime valuationDate) {
    _cached.clear();
    final Map<String, InstrumentDefinition<?>[]> definitionsForCurves = new HashMap<>();
    for (int i = 0; i < _curveNames.size(); i++) {
      final String[] curveNamesForUnit = _curveNames.get(i);
      for (final String curveNameForUnit : curveNamesForUnit) {
        final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodes = _nodes.get(curveNameForUnit);
        final Iterator<Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodesIterator = nodes.entrySet().iterator();
        final int nNodes = nodes.size();
        final InstrumentDefinition<?>[] definitions = new InstrumentDefinition[nNodes];
        for (int k = 0; k < nNodes; k++) {
          final Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double> info = nodesIterator.next();
          definitions[k] = info.getKey().getFirst().generateInstrument(valuationDate, info.getValue(), 1, info.getKey().getSecond());
        }
        definitionsForCurves.put(curveNameForUnit, definitions);
      }
    }
    return definitionsForCurves;
  }

  public CurveBuilder<T> replaceMarketQuote(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketQuote) {
    final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = _nodes.get(curveName);
    if (nodesForCurve == null) {
      throw new IllegalStateException();
    }
    final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesWithReplacedPoint = new LinkedHashMap<>(nodesForCurve);
    final Double replacedPoint = nodesWithReplacedPoint.put(Pairs.of(instrumentGenerator, attributeGenerator), marketQuote);
    if (replacedPoint == null) {
      throw new IllegalStateException();
    }
    final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> newNodesForCurve = new HashMap<>(_nodes);
    newNodesForCurve.put(curveName, nodesWithReplacedPoint);
    return replaceMarketQuote(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, newNodesForCurve, _curveGenerators, _knownData, _knownBundle, _fixingTs);
  }

  abstract CurveBuilder<T> replaceMarketQuote(List<String[]> curveNames, LinkedHashMap<String, Currency> discountingCurves,
      LinkedHashMap<String, IborIndex[]> iborCurves, LinkedHashMap<String, IndexON[]> overnightCurves,
      Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes, Map<String, ? extends CurveTypeSetUpInterface<T>> curveGenerators,
      T knownData, CurveBuildingBlockBundle knownBundle, Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs);

  List<String[]> getCurveNames() {
    return _curveNames;
  }

  LinkedHashMap<String, Currency> getDiscountingCurves() {
    return _discountingCurves;
  }

  LinkedHashMap<String, IborIndex[]> getIborCurves() {
    return _iborCurves;
  }

  LinkedHashMap<String, IndexON[]> getOvernightCurves() {
    return _overnightCurves;
  }

  Map<String, ? extends CurveTypeSetUpInterface<T>> getCurveGenerators() {
    return _curveGenerators;
  }

  CurveBuildingBlockBundle getKnownBundle() {
    return _knownBundle;
  }

  Map<Index, ZonedDateTimeDoubleTimeSeries> getFixingTs() {
    return _fixingTs;
  }

  T getKnownData() {
    return _knownData;
  }

  Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> getNodes() {
    return _nodes;
  }

  Map<ZonedDateTime, MultiCurveBundle[]> getCached() {
    return _cached;
  }


}
