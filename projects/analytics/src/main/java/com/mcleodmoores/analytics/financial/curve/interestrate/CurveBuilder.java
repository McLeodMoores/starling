/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public abstract class CurveBuilder<T extends ParameterProviderInterface> {
  private final List<List<String>> _curveNames;
  private final List<Pair<String, UniqueIdentifiable>> _discountingCurves;
  private final List<Pair<String, List<IborTypeIndex>>> _iborCurves;
  private final List<Pair<String, List<OvernightIndex>>> _overnightCurves;
  private final Map<String, ? extends CurveTypeSetUpInterface> _curveGenerators;
  private final CurveBuildingBlockBundle _knownBundle;
  private final T _knownData;
  private final Map<String, List<InstrumentDefinition<?>>> _nodes;
  private final Map<ZonedDateTime, MultiCurveBundle[]> _cached;

  CurveBuilder(final List<List<String>> curveNames,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface> curveGenerators,
      final T knownData, final CurveBuildingBlockBundle knownBundle) {
    _curveNames = new ArrayList<>(curveNames);
    _discountingCurves = new ArrayList<>(discountingCurves);
    _iborCurves = new ArrayList<>(iborCurves);
    _overnightCurves = new ArrayList<>(overnightCurves);
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
        final List<String> curveNamesForUnit = _curveNames.get(i);
        final SingleCurveBundle[] unitBundle = new SingleCurveBundle[curveNamesForUnit.size()];
        for (int j = 0; j < curveNamesForUnit.size(); j++) {
          final String curveName = curveNamesForUnit.get(j);
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
      List<Pair<String, UniqueIdentifiable>> discountingCurves, List<Pair<String, List<IborTypeIndex>>> iborCurves,
      List<Pair<String, List<OvernightIndex>>> overnightCurves);

  public Map<String, InstrumentDefinition<?>[]> getDefinitionsForCurves(final ZonedDateTime valuationDate) {
    _cached.clear();
    final Map<String, InstrumentDefinition<?>[]> definitionsForCurves = new HashMap<>();
    for (int i = 0; i < _curveNames.size(); i++) {
      final List<String> curveNamesForUnit = _curveNames.get(i);
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

  List<List<String>> getCurveNames() {
    return _curveNames;
  }

  List<Pair<String, UniqueIdentifiable>> getDiscountingCurves() {
    return _discountingCurves;
  }

  List<Pair<String, List<IborTypeIndex>>> getIborCurves() {
    return _iborCurves;
  }

  List<Pair<String, List<OvernightIndex>>> getOvernightCurves() {
    return _overnightCurves;
  }

  Map<String, ? extends CurveTypeSetUpInterface> getCurveGenerators() {
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
