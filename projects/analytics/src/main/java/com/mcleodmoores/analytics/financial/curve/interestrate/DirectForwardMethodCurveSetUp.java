/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class DirectForwardMethodCurveSetUp implements CurveSetUpInterface<MulticurveProviderForward> {
  //TODO method that takes definitions
  protected final List<String[]> _curveNames;
  //TODO should these live in curve type setup?
  protected final LinkedHashMap<String, Currency> _discountingCurves;
  protected final LinkedHashMap<String, IborTypeIndex[]> _iborCurves;
  protected final LinkedHashMap<String, OvernightIndex[]> _overnightCurves;
  protected final Map<String, DirectForwardMethodCurveTypeSetUp> _curveTypes;
  protected final Map<String, List<InstrumentDefinition<?>>> _nodes;
  protected MulticurveProviderForward _knownData;
  protected CurveBuildingBlockBundle _knownBundle;

  protected DirectForwardMethodCurveSetUp() {
    _curveNames = new ArrayList<>();
    _discountingCurves = new LinkedHashMap<>();
    _iborCurves = new LinkedHashMap<>();
    _overnightCurves = new LinkedHashMap<>();
    _curveTypes = new HashMap<>();
    //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
    _nodes = new LinkedHashMap<>();
    _knownData = null;
    _knownBundle = null;
  }

  protected DirectForwardMethodCurveSetUp(final DirectForwardMethodCurveSetUp setup) {
    //TODO copy
    _curveNames = setup._curveNames;
    _discountingCurves = setup._discountingCurves;
    _iborCurves = setup._iborCurves;
    _overnightCurves = setup._overnightCurves;
    _curveTypes = setup._curveTypes;
    _nodes = setup._nodes;
    _knownData = setup._knownData;
    _knownBundle = setup._knownBundle;
  }

  protected DirectForwardMethodCurveSetUp(final List<String[]> curveNames,
      final LinkedHashMap<String, Currency> discountingCurves,
      final LinkedHashMap<String, IborTypeIndex[]> iborCurves,
      final LinkedHashMap<String, OvernightIndex[]> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, DirectForwardMethodCurveTypeSetUp> curveTypes,
      final MulticurveProviderForward knownData,
      final CurveBuildingBlockBundle knownBundle) {
    _curveNames = new ArrayList<>(curveNames);
    _discountingCurves = new LinkedHashMap<>(discountingCurves);
    _iborCurves = new LinkedHashMap<>(iborCurves);
    _overnightCurves = new LinkedHashMap<>(overnightCurves);
    _nodes = new HashMap<>(nodes);
    _curveTypes = new HashMap<>(curveTypes);
    _knownData = knownData == null ? null : knownData.copy();
    _knownBundle = knownBundle == null ? null : knownBundle; //TODO no copy
  }


  @Override
  public DirectForwardMethodCurveBuilder getBuilder() {
    return new DirectForwardMethodCurveBuilder(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _curveTypes,
        _knownData, _knownBundle);
  }

  @Override
  public DirectForwardMethodCurveSetUp copy() {
    return new DirectForwardMethodCurveSetUp(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _curveTypes,
        _knownData, _knownBundle);
  }

  @Override
  public DirectForwardMethodCurveSetUp building(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(curveNames);
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public DirectForwardMethodCurveSetUp buildingFirst(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(curveNames);
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public DirectForwardMethodCurveSetUp thenBuilding(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      throw new IllegalStateException();
    }
    _curveNames.add(curveNames);
    return this;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp using(final String curveName) {
    final DirectForwardMethodCurveTypeSetUp type = new DirectForwardMethodCurveTypeSetUp(curveName, this);
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
  public DirectForwardMethodCurveSetUp removeNodes(final String curveName) {
    _nodes.put(curveName, null);
    return this;
  }

  //TODO add a withNode that takes definitions

  @Override
  public DirectForwardMethodCurveSetUp withKnownData(final MulticurveProviderForward knownData) {
    // probably better to merge this
    _knownData = knownData;
    return this;
  }

  @Override
  public DirectForwardMethodCurveSetUp withKnownBundle(final CurveBuildingBlockBundle knownBundle) {
    _knownBundle = knownBundle;
    return this;
  }

}
