/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.ArrayList;
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
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public abstract class CurveBuilder<T extends ParameterProviderInterface> {
  private final List<String[]> _curveNames;
  private final LinkedHashMap<String, Currency> _discountingCurves;
  private final LinkedHashMap<String, IborTypeIndex[]> _iborCurves;
  private final LinkedHashMap<String, OvernightIndex[]> _overnightCurves;
  private final Map<String, ? extends CurveTypeSetUpInterface<T>> _curveGenerators;
  private final CurveBuildingBlockBundle _knownBundle;
  private final T _knownData;
  private final Map<String, List<InstrumentDefinition<?>>> _nodes;
  private final Map<ZonedDateTime, MultiCurveBundle[]> _cached;

  CurveBuilder(final List<String[]> curveNames,
      final LinkedHashMap<String, Currency> discountingCurves,
      final LinkedHashMap<String, IborTypeIndex[]> iborCurves,
      final LinkedHashMap<String, OvernightIndex[]> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface<T>> curveGenerators,
      final T knownData, final CurveBuildingBlockBundle knownBundle) {
    _curveNames = new ArrayList<>(curveNames);
    _discountingCurves = new LinkedHashMap<>(discountingCurves);
    _iborCurves = new LinkedHashMap<>(iborCurves);
    _overnightCurves = new LinkedHashMap<>(overnightCurves);
    _nodes = new HashMap<>(nodes);
    _curveGenerators = new HashMap<>(curveGenerators);
    _knownData = knownData == null ? null : (T) knownData.copy();
    _knownBundle = knownBundle; // TODO copy this
    _cached = new HashMap<>();
  }

  //TODO cache definitions on LocalDate
  public Pair<T, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixings) {
    MultiCurveBundle<GeneratorYDCurve>[] curveBundles = _cached.get(valuationDate);
    if (curveBundles == null) {
      final Map<String, GeneratorYDCurve> generatorForCurve = new HashMap<>();
      curveBundles = new MultiCurveBundle[_curveNames.size()];
      for (int i = 0; i < _curveNames.size(); i++) {
        final String[] curveNamesForUnit = _curveNames.get(i);
        final SingleCurveBundle[] unitBundle = new SingleCurveBundle[curveNamesForUnit.length];
        for (int j = 0; j < curveNamesForUnit.length; j++) {
          final String curveName = curveNamesForUnit[j];
          final List<InstrumentDefinition<?>> nodesForCurve = _nodes.get(curveName);
          if (nodesForCurve == null) {
            throw new IllegalStateException("No nodes found for curve called " + curveName);
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
      LinkedHashMap<String, Currency> discountingCurves, LinkedHashMap<String, IborTypeIndex[]> iborCurves, LinkedHashMap<String, OvernightIndex[]> overnightCurves);

  public Map<String, InstrumentDefinition<?>[]> getDefinitionsForCurves(final ZonedDateTime valuationDate) {
    _cached.clear();
    final Map<String, InstrumentDefinition<?>[]> definitionsForCurves = new HashMap<>();
    for (int i = 0; i < _curveNames.size(); i++) {
      final String[] curveNamesForUnit = _curveNames.get(i);
      for (final String curveNameForUnit : curveNamesForUnit) {
        final List<InstrumentDefinition<?>> nodes = _nodes.get(curveNameForUnit);
        final int nNodes = nodes.size();
        final InstrumentDefinition<?>[] definitions = new InstrumentDefinition[nNodes];
        for (int k = 0; k < nNodes; k++) {
          definitions[k] = nodes.get(k);
        }
        definitionsForCurves.put(curveNameForUnit, definitions);
      }
    }
    return definitionsForCurves;
  }

  List<String[]> getCurveNames() {
    return _curveNames;
  }

  LinkedHashMap<String, Currency> getDiscountingCurves() {
    return _discountingCurves;
  }

  LinkedHashMap<String, IborTypeIndex[]> getIborCurves() {
    return _iborCurves;
  }

  LinkedHashMap<String, OvernightIndex[]> getOvernightCurves() {
    return _overnightCurves;
  }

  Map<String, ? extends CurveTypeSetUpInterface<T>> getCurveGenerators() {
    return _curveGenerators;
  }

  CurveBuildingBlockBundle getKnownBundle() {
    return _knownBundle;
  }

  T getKnownData() {
    return _knownData;
  }

  Map<String, List<InstrumentDefinition<?>>> getNodes() {
    return _nodes;
  }

  Map<ZonedDateTime, MultiCurveBundle[]> getCached() {
    return _cached;
  }


}
