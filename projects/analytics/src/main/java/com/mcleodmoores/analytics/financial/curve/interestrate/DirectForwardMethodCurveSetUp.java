/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class DirectForwardMethodCurveSetUp implements CurveSetUpInterface {
  private final List<List<String>> _curveNames;
  private final Map<String, DirectForwardMethodCurveTypeSetUp> _curveTypes;
  private final Map<DirectForwardMethodPreConstructedCurveTypeSetUp, YieldAndDiscountCurve> _preConstructedCurves;
  private final Map<String, List<InstrumentDefinition<?>>> _nodes;
  private FXMatrix _fxMatrix;
  private CurveBuildingBlockBundle _knownBundle;
  private double _absoluteTolerance = 1e-12;
  private double _relativeTolerance = 1e-12;
  private int _maxSteps = 100;

  protected DirectForwardMethodCurveSetUp() {
    _curveNames = new ArrayList<>();
    _curveTypes = new HashMap<>();
    _preConstructedCurves = new HashMap<>();
    _nodes = new LinkedHashMap<>();
    _fxMatrix = new FXMatrix();
    _knownBundle = null;
  }

  protected DirectForwardMethodCurveSetUp(final DirectForwardMethodCurveSetUp setup) {
    //TODO copy
    _curveNames = setup._curveNames;
    _curveTypes = setup._curveTypes;
    _preConstructedCurves = setup._preConstructedCurves;
    _nodes = setup._nodes;
    _fxMatrix = setup._fxMatrix;
    _knownBundle = setup._knownBundle;
    _absoluteTolerance = setup._absoluteTolerance;
    _relativeTolerance = setup._relativeTolerance;
    _maxSteps = setup._maxSteps;
  }

  protected DirectForwardMethodCurveSetUp(final List<List<String>> curveNames,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, DirectForwardMethodCurveTypeSetUp> curveTypes,
      final Map<DirectForwardMethodPreConstructedCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
      final FXMatrix fxMatrix,
      final CurveBuildingBlockBundle knownBundle,
      final double absoluteTolerance,
      final double relativeTolerance,
      final int maxSteps) {
    _curveNames = new ArrayList<>(curveNames);
    _nodes = new HashMap<>(nodes);
    _curveTypes = new HashMap<>(curveTypes);
    _preConstructedCurves = new HashMap<>(preConstructedCurves);
    _fxMatrix = fxMatrix;
    _knownBundle = knownBundle;
    _absoluteTolerance = absoluteTolerance;
    _relativeTolerance = relativeTolerance;
    _maxSteps = maxSteps;
  }

  @Override
  public DirectForwardMethodCurveBuilder getBuilder() {
    final List<Pair<String, UniqueIdentifiable>> discountingCurves = new ArrayList<>();
    final List<Pair<String, List<IborTypeIndex>>> iborCurves = new ArrayList<>();
    final List<Pair<String, List<OvernightIndex>>> overnightCurves = new ArrayList<>();
    for (final Map.Entry<String, DirectForwardMethodCurveTypeSetUp> entry : _curveTypes.entrySet()) {
      final String curveName = entry.getKey();
      final DirectForwardMethodCurveTypeSetUp setUp = entry.getValue();
      final UniqueIdentifiable discountingCurveId = setUp.getDiscountingCurveId();
      if (discountingCurveId != null) {
        discountingCurves.add(Pairs.of(curveName, discountingCurveId));
      }
      final List<IborTypeIndex> iborCurveIndices = setUp.getIborCurveIndices();
      if (iborCurveIndices != null) {
        iborCurves.add(Pairs.of(curveName, iborCurveIndices));
      }
      final List<OvernightIndex> overnightCurveIndices = setUp.getOvernightCurveIndices();
      if (overnightCurveIndices != null) {
        overnightCurves.add(Pairs.of(curveName, overnightCurveIndices));
      }
    }
    final Map<Currency, YieldAndDiscountCurve> knownDiscountingCurves = new HashMap<>();
    final Map<IborIndex, DoublesCurve> knownIborCurves = new HashMap<>();
    final Map<IndexON, YieldAndDiscountCurve> knownOvernightCurves = new HashMap<>();
    for (final Map.Entry<DirectForwardMethodPreConstructedCurveTypeSetUp, YieldAndDiscountCurve> entry : _preConstructedCurves.entrySet()) {
      final DirectForwardMethodPreConstructedCurveTypeSetUp setUp = entry.getKey();
      final YieldAndDiscountCurve curve = entry.getValue();
      if (!(curve instanceof YieldCurve)) {
        throw new IllegalStateException(); // TODO this check should not be done here
      }
      final UniqueIdentifiable discountingCurveId = setUp.getDiscountingCurveId();
      if (discountingCurveId != null) {
        knownDiscountingCurves.put((Currency) discountingCurveId, curve);
      }
      final List<IborTypeIndex> iborCurveIndices = setUp.getIborCurveIndices();
      if (iborCurveIndices != null) {
        for (final IborTypeIndex index : iborCurveIndices) {
          knownIborCurves.put(IndexConverter.toIborIndex(index), ((YieldCurve) curve).getCurve());
        }
      }
      final List<OvernightIndex> overnightCurveIndices = setUp.getOvernightCurveIndices();
      if (overnightCurveIndices != null) {
        for (final OvernightIndex index : overnightCurveIndices) {
          knownOvernightCurves.put(IndexConverter.toIndexOn(index), curve);
        }
      }
    }
    final MulticurveProviderForward knownData = new MulticurveProviderForward(knownDiscountingCurves, knownIborCurves, knownOvernightCurves, _fxMatrix);
    return new DirectForwardMethodCurveBuilder(_curveNames, discountingCurves, iborCurves, overnightCurves, _nodes, _curveTypes, knownData, _knownBundle,
        _absoluteTolerance, _relativeTolerance, _maxSteps);
  }

  @Override
  public DirectForwardMethodCurveSetUp copy() {
    return new DirectForwardMethodCurveSetUp(_curveNames, _nodes, _curveTypes, _preConstructedCurves, _fxMatrix, _knownBundle,
        _absoluteTolerance, _relativeTolerance, _maxSteps);
  }

  @Override
  public DirectForwardMethodCurveSetUp building(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(Arrays.asList(curveNames));
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public DirectForwardMethodCurveSetUp buildingFirst(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(Arrays.asList(curveNames));
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public DirectForwardMethodCurveSetUp thenBuilding(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      throw new IllegalStateException();
    }
    _curveNames.add(Arrays.asList(curveNames));
    return this;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp using(final String curveName) {
    final DirectForwardMethodCurveTypeSetUp type = new DirectForwardMethodCurveTypeSetUp(this);
    final Object replaced = _curveTypes.put(curveName, type);
    if (replaced != null) {
      throw new IllegalStateException();
    }
    return type;
  }

  @Override
  public DirectForwardMethodCurveSetUp addNode(final String curveName, final InstrumentDefinition<?> definition) {
    List<InstrumentDefinition<?>> nodesForCurve = _nodes.get(curveName);
    if (nodesForCurve == null) {
      nodesForCurve = new ArrayList<>();
      _nodes.put(curveName, nodesForCurve);
    }
    nodesForCurve.add(definition);
    //TODO if market data is already present, log then overwrite
    return this;
  }

  @Override
  public DirectForwardMethodCurveSetUp addFxMatrix(final FXMatrix fxMatrix) {
    _fxMatrix = fxMatrix;
    return this;
  }

  @Override
  public DirectForwardMethodCurveSetUp removeNodes(final String curveName) {
    _nodes.put(curveName, null);
    return this;
  }

  @Override
  public DirectForwardMethodCurveSetUp withKnownBundle(final CurveBuildingBlockBundle knownBundle) {
    _knownBundle = knownBundle;
    return this;
  }

  @Override
  public PreConstructedCurveTypeSetUp using(final YieldAndDiscountCurve curve) {
    final DirectForwardMethodPreConstructedCurveTypeSetUp type = new DirectForwardMethodPreConstructedCurveTypeSetUp(this);
    final Object replaced = _preConstructedCurves.put(type, curve);
    if (replaced != null) {
      throw new IllegalStateException();
    }
    return type;
  }


  @Override
  public CurveSetUpInterface rootFindingAbsoluteTolerance(final double tolerance) {
    _absoluteTolerance = tolerance;
    return this;
  }

  @Override
  public CurveSetUpInterface rootFindingRelativeTolerance(final double tolerance) {
    _relativeTolerance = tolerance;
    return this;
  }

  @Override
  public CurveSetUpInterface rootFindingMaximumSteps(final int maxSteps) {
    _maxSteps = maxSteps;
    return this;
  }
}
