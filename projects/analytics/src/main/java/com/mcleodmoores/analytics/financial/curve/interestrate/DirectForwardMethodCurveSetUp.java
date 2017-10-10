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

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;

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
  }

  protected DirectForwardMethodCurveSetUp(final List<List<String>> curveNames,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, DirectForwardMethodCurveTypeSetUp> curveTypes,
      final Map<DirectForwardMethodPreConstructedCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
      final FXMatrix fxMatrix,
      final CurveBuildingBlockBundle knownBundle) {
    _curveNames = new ArrayList<>(curveNames);
    _nodes = new HashMap<>(nodes);
    _curveTypes = new HashMap<>(curveTypes);
    _preConstructedCurves = new HashMap<>(preConstructedCurves);
    _fxMatrix = fxMatrix;
    _knownBundle = knownBundle;
  }


  @Override
  public DirectForwardMethodCurveBuilder getBuilder() {
    final MulticurveProviderForward knownData;
    if (_knownData != null) {
      knownData = _knownData;
    } else {
      knownData = new MulticurveProviderForward(_fxMatrix);
    }
    return new DirectForwardMethodCurveBuilder(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _curveTypes,
        knownData, _knownBundle);
  }

  @Override
  public DirectForwardMethodCurveSetUp copy() {
    return new DirectForwardMethodCurveSetUp(_curveNames, _nodes, _curveTypes, _preConstructedCurves, _fxMatrix, _knownBundle);
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

}
