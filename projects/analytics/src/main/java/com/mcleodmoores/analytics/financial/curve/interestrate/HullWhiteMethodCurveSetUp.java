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
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class HullWhiteMethodCurveSetUp implements CurveSetUpInterface<HullWhiteOneFactorProviderDiscount> {
  //TODO method that takes definitions
  private final List<List<String>> _curveNames;
  //TODO should these live in curve type setup?
  private final List<Pair<String, UniqueIdentifiable>> _discountingCurves;
  private final List<Pair<String, List<IborTypeIndex>>> _iborCurves;
  private final List<Pair<String, List<OvernightIndex>>> _overnightCurves;
  private final Map<String, HullWhiteMethodCurveTypeSetUp> _curveTypes;
  private FXMatrix _fxMatrix;
  private final Map<String, List<InstrumentDefinition<?>>> _nodes;
  private HullWhiteOneFactorProviderDiscount _knownData;
  private CurveBuildingBlockBundle _knownBundle;

  protected HullWhiteMethodCurveSetUp() {
    _curveNames = new ArrayList<>();
    _discountingCurves = new ArrayList<>();
    _iborCurves = new ArrayList<>();
    _overnightCurves = new ArrayList<>();
    _curveTypes = new HashMap<>();
    //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
    _nodes = new LinkedHashMap<>();
    _fxMatrix = new FXMatrix();
    _knownData = null;
    _knownBundle = null;
  }

  protected HullWhiteMethodCurveSetUp(final HullWhiteMethodCurveSetUp setup) {
    //TODO copy
    _curveNames = setup._curveNames;
    _discountingCurves = setup._discountingCurves;
    _iborCurves = setup._iborCurves;
    _overnightCurves = setup._overnightCurves;
    _curveTypes = setup._curveTypes;
    //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
    _nodes = setup._nodes;
    _fxMatrix = setup._fxMatrix;
    _knownData = setup._knownData;
    _knownBundle = setup._knownBundle;
  }

  protected HullWhiteMethodCurveSetUp(final List<List<String>> curveNames, final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, HullWhiteMethodCurveTypeSetUp> curveTypes,
      final FXMatrix fxMatrix,
      final HullWhiteOneFactorProviderDiscount knownData,
      final CurveBuildingBlockBundle knownBundle) {
    _curveNames = new ArrayList<>(curveNames);
    _discountingCurves = new ArrayList<>(discountingCurves);
    _iborCurves = new ArrayList<>(iborCurves);
    _overnightCurves = new ArrayList<>(overnightCurves);
    _nodes = new HashMap<>(nodes);
    _curveTypes = new HashMap<>(curveTypes);
    _fxMatrix = fxMatrix;
    _knownData = knownData == null ? null : knownData.copy();
    _knownBundle = knownBundle == null ? null : knownBundle; //TODO no copy
  }


  @Override
  public HullWhiteMethodCurveBuilder getBuilder() {
    final HullWhiteOneFactorProviderDiscount knownData;
    if (_knownData != null) {
      knownData = _knownData;
    } else {
      knownData = null; // TODO add constants etc. new HullWhiteOneFactorProviderDiscount(new MulticurveProviderDiscount(fxMatrix));
    }
    return new HullWhiteMethodCurveBuilder(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _curveTypes,
        knownData, _knownBundle);
  }

  @Override
  public HullWhiteMethodCurveSetUp copy() {
    return new HullWhiteMethodCurveSetUp(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _curveTypes,
        _fxMatrix, _knownData, _knownBundle);
  }

  @Override
  public HullWhiteMethodCurveSetUp building(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(Arrays.asList(curveNames));
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public HullWhiteMethodCurveSetUp buildingFirst(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(Arrays.asList(curveNames));
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public HullWhiteMethodCurveSetUp thenBuilding(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      throw new IllegalStateException();
    }
    _curveNames.add(Arrays.asList(curveNames));
    return this;
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp using(final String curveName) {
    final HullWhiteMethodCurveTypeSetUp type = new HullWhiteMethodCurveTypeSetUp(curveName, this);
    final Object replaced = _curveTypes.put(curveName, type);
    if (replaced != null) {
      throw new IllegalStateException();
    }
    return type;
  }

  @Override
  public HullWhiteMethodCurveSetUp addNode(final String curveName, final InstrumentDefinition<?> definition) {
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
  public HullWhiteMethodCurveSetUp addFxMatrix(final FXMatrix fxMatrix) {
    if (_knownData != null) {
      throw new IllegalStateException();
    }
    _fxMatrix = fxMatrix;
    return this;
  }

  @Override
  public HullWhiteMethodCurveSetUp removeNodes(final String curveName) {
    _nodes.put(curveName, null);
    return this;
  }

  @Override
  public HullWhiteMethodCurveSetUp withKnownData(final HullWhiteOneFactorProviderDiscount knownData) {
    if (_fxMatrix != null) {
      throw new IllegalStateException();
    }
    // probably better to merge this
    _knownData = knownData;
    return this;
  }

  @Override
  public HullWhiteMethodCurveSetUp withKnownBundle(final CurveBuildingBlockBundle knownBundle) {
    _knownBundle = knownBundle;
    return this;
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

  Map<String, HullWhiteMethodCurveTypeSetUp> getCurveTypes() {
    return _curveTypes;
  }

  Map<String, List<InstrumentDefinition<?>>> getNodes() {
    return _nodes;
  }

  HullWhiteOneFactorProviderDiscount getKnownData() {
    return _knownData;
  }

  CurveBuildingBlockBundle getKnownBundle() {
    return _knownBundle;
  }


}
