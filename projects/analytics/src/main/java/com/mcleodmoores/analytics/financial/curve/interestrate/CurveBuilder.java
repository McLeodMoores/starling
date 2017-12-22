/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.LinkedListMultimap;
import com.mcleodmoores.analytics.financial.curve.CurveUtils;
import com.mcleodmoores.analytics.financial.curve.CurveUtils.NodeOrderCalculator;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.Index;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public abstract class CurveBuilder<T extends ParameterProviderInterface> {
  private final List<List<String>> _curveNames;
  private final List<Pair<String, UniqueIdentifiable>> _discountingCurves;
  private final List<Pair<String, List<IborTypeIndex>>> _iborCurves;
  private final List<Pair<String, List<OvernightIndex>>> _overnightCurves;
  private final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> _issuerCurves;
  private final Map<String, ? extends CurveTypeSetUpInterface> _curveTypes;
  private final FXMatrix _fxMatrix;
  private final Map<Currency, YieldAndDiscountCurve> _knownDiscountingCurves;
  private final Map<IborIndex, YieldAndDiscountCurve> _knownIborCurves;
  private final Map<IndexON, YieldAndDiscountCurve> _knownOvernightCurves;
  private final CurveBuildingBlockBundle _knownBundle;
  private final Map<String, List<InstrumentDefinition<?>>> _nodes;

  CurveBuilder(final List<List<String>> curveNames,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface> curveTypes,
      final FXMatrix fxMatrix,
      final Map<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
      final CurveBuildingBlockBundle knownBundle) {
    this(curveNames, discountingCurves, iborCurves, overnightCurves, null, nodes, curveTypes, fxMatrix, preConstructedCurves, knownBundle);
  }

  CurveBuilder(final List<List<String>> curveNames,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final List<Pair<String, List<Pair<Object, LegalEntityFilter<LegalEntity>>>>> issuerCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface> curveTypes,
      final FXMatrix fxMatrix,
      final Map<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
      final CurveBuildingBlockBundle knownBundle) {
    _curveNames = new ArrayList<>(ArgumentChecker.notEmpty(curveNames, "curveNames"));
    _discountingCurves = new ArrayList<>(ArgumentChecker.notNull(discountingCurves, "discountingCurves"));
    _iborCurves = new ArrayList<>(ArgumentChecker.notNull(iborCurves, "iborCurves"));
    _overnightCurves = new ArrayList<>(ArgumentChecker.notNull(overnightCurves, "overnightCurves"));
    _issuerCurves = LinkedListMultimap.create();
    if (issuerCurves != null) {
      for (final Pair<String, List<Pair<Object, LegalEntityFilter<LegalEntity>>>> issuerCurve : issuerCurves) {
        _issuerCurves.put(issuerCurve.getKey(), issuerCurve.getValue().get(0)); //TODO only one handled
      }
    }
    _nodes = new HashMap<>(ArgumentChecker.notEmpty(nodes, "nodes"));
    _curveTypes = new HashMap<>(ArgumentChecker.notEmpty(curveTypes, "curveTypes"));
    _fxMatrix = fxMatrix == null ? new FXMatrix() : fxMatrix;
    _knownDiscountingCurves = new HashMap<>();
    _knownIborCurves = new HashMap<>();
    _knownOvernightCurves = new HashMap<>();
    for (final Map.Entry<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> entry : preConstructedCurves.entrySet()) {
      final PreConstructedCurveTypeSetUp setUp = entry.getKey();
      final YieldAndDiscountCurve curve = entry.getValue();
      final UniqueIdentifiable discountingCurveId = setUp.getDiscountingCurveId();
      if (discountingCurveId != null) {
        _knownDiscountingCurves.put((Currency) discountingCurveId, curve); //TODO cast
      }
      final List<IborTypeIndex> iborCurveIndices = setUp.getIborCurveIndices();
      if (iborCurveIndices != null) {
        for (final IborTypeIndex index : iborCurveIndices) {
          _knownIborCurves.put(IndexConverter.toIborIndex(index), curve);
        }
      }
      final List<OvernightIndex> overnightCurveIndices = setUp.getOvernightCurveIndices();
      if (overnightCurveIndices != null) {
        for (final OvernightIndex index : overnightCurveIndices) {
          _knownOvernightCurves.put(IndexConverter.toIndexOn(index), curve);
        }
      }
    }
    if (knownBundle == null) {
      _knownBundle = null;
    } else {
      _knownBundle = new CurveBuildingBlockBundle();
      _knownBundle.addAll(knownBundle);
    }
  }

  public Pair<T, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixings) {
    MultiCurveBundle<GeneratorYDCurve>[] curveBundles = null;
    final Map<String, GeneratorYDCurve> generatorForCurve = new HashMap<>();
    curveBundles = new MultiCurveBundle[_curveNames.size()];
    for (int i = 0; i < _curveNames.size(); i++) {
      final List<String> curveNamesForUnit = _curveNames.get(i);
      final SingleCurveBundle[] unitBundle = new SingleCurveBundle[curveNamesForUnit.size()];
      for (int j = 0; j < curveNamesForUnit.size(); j++) {
        final String curveName = curveNamesForUnit.get(j);
        // TODO sensible behaviour if not set
        final NodeOrderCalculator nodeOrderCalculator = new CurveUtils.NodeOrderCalculator(_curveTypes.get(curveName).getNodeTimeCalculator());
        final List<InstrumentDefinition<?>> nodesForCurve = _nodes.get(curveName);
        if (nodesForCurve == null) {
          throw new IllegalStateException("No nodes found for curve called " + curveName);
        }
        final int nNodes = nodesForCurve.size();
        final InstrumentDerivative[] instruments = new InstrumentDerivative[nNodes];
        final double[] curveInitialGuess = new double[nNodes];
        for (int k = 0; k < nNodes; k++) {
          final InstrumentDefinition<?> definition = nodesForCurve.get(k);
          instruments[k] = CurveUtils.convert(definition, fixings, valuationDate);
        }
        Arrays.sort(instruments, nodeOrderCalculator);
        for (int k = 0; k < nNodes; k++) {
          curveInitialGuess[k] = instruments[k].accept(CurveUtils.RATES_INITIALIZATION);
        }
        final GeneratorYDCurve instrumentGenerator = _curveTypes.get(curveName).buildCurveGenerator(valuationDate).finalGenerator(instruments);
        generatorForCurve.put(curveName, instrumentGenerator);
        unitBundle[j] = new SingleCurveBundle<>(curveName, instruments, instrumentGenerator.initialGuess(curveInitialGuess), instrumentGenerator);
      }
      curveBundles[i] = new MultiCurveBundle<>(unitBundle);
    }
    return buildCurves(curveBundles, _knownBundle, _discountingCurves, _iborCurves, _overnightCurves, _fxMatrix, _knownDiscountingCurves,
        _knownIborCurves, _knownOvernightCurves);
  }

  abstract Pair<T, CurveBuildingBlockBundle> buildCurves(
      MultiCurveBundle[] curveBundles,
      CurveBuildingBlockBundle knownBundle,
      List<Pair<String, UniqueIdentifiable>> discountingCurves,
      List<Pair<String, List<IborTypeIndex>>> iborCurves,
      List<Pair<String, List<OvernightIndex>>> overnightCurves,
      FXMatrix fxMatrix,
      Map<Currency, YieldAndDiscountCurve> knownDiscountingCurves,
      Map<IborIndex, YieldAndDiscountCurve> knownIborCurves,
      Map<IndexON, YieldAndDiscountCurve> knownOvernightCurves);


  public Map<String, InstrumentDefinition<?>[]> getDefinitionsForCurves() {
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
    return _curveTypes;
  }

  Map<String, List<InstrumentDefinition<?>>> getNodes() {
    return _nodes;
  }

  Map<Currency, YieldAndDiscountCurve> getKnownDiscountingCurves() {
    return _knownDiscountingCurves;
  }

  Map<IborIndex, YieldAndDiscountCurve> getKnownIborCurves() {
    return _knownIborCurves;
  }

  Map<IndexON, YieldAndDiscountCurve> getKnownOvernightCurves() {
    return _knownOvernightCurves;
  }

  FXMatrix getFxMatrix() {
    return _fxMatrix;
  }

  CurveBuildingBlockBundle getKnownBundle() {
    return _knownBundle;
  }

  Map<String, ? extends CurveTypeSetUpInterface> getCurveTypes() {
    return _curveTypes;
  }
}
