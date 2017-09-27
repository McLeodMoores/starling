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
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class HullWhiteMethodCurveSetUp implements CurveSetUpInterface<HullWhiteOneFactorProviderDiscount> {
  //TODO method that takes definitions
  protected final List<String[]> _curveNames;
  //TODO should these live in curve type setup?
  protected final LinkedHashMap<String, Currency> _discountingCurves;
  protected final LinkedHashMap<String, IborTypeIndex[]> _iborCurves;
  protected final LinkedHashMap<String, OvernightIndex[]> _overnightCurves;
  protected final Map<String, HullWhiteMethodCurveTypeSetUp> _curveTypes;
  protected final Map<String, List<InstrumentDefinition<?>>> _nodes;
  protected HullWhiteOneFactorProviderDiscount _knownData;
  protected CurveBuildingBlockBundle _knownBundle;

  protected HullWhiteMethodCurveSetUp() {
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

  protected HullWhiteMethodCurveSetUp(final HullWhiteMethodCurveSetUp setup) {
    //TODO copy
    _curveNames = setup._curveNames;
    _discountingCurves = setup._discountingCurves;
    _iborCurves = setup._iborCurves;
    _overnightCurves = setup._overnightCurves;
    _curveTypes = setup._curveTypes;
    //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
    _nodes = setup._nodes;
    _knownData = setup._knownData;
    _knownBundle = setup._knownBundle;
  }

  protected HullWhiteMethodCurveSetUp(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborTypeIndex[]> iborCurves,
      final LinkedHashMap<String, OvernightIndex[]> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, HullWhiteMethodCurveTypeSetUp> curveTypes,  final HullWhiteOneFactorProviderDiscount knownData,
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
  public HullWhiteMethodCurveBuilder getBuilder() {
    return new HullWhiteMethodCurveBuilder(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _curveTypes,
        _knownData, _knownBundle);
  }

  @Override
  public HullWhiteMethodCurveSetUp copy() {
    return new HullWhiteMethodCurveSetUp(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _curveTypes,
        _knownData, _knownBundle);
  }

  @Override
  public HullWhiteMethodCurveSetUp building(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(curveNames);
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public HullWhiteMethodCurveSetUp buildingFirst(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(curveNames);
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public HullWhiteMethodCurveSetUp thenBuilding(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      throw new IllegalStateException();
    }
    _curveNames.add(curveNames);
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
  public HullWhiteMethodCurveSetUp removeNodes(final String curveName) {
    _nodes.put(curveName, null);
    return this;
  }

  @Override
  public HullWhiteMethodCurveSetUp withKnownData(final HullWhiteOneFactorProviderDiscount knownData) {
    // probably better to merge this
    _knownData = knownData;
    return this;
  }

  @Override
  public HullWhiteMethodCurveSetUp withKnownBundle(final CurveBuildingBlockBundle knownBundle) {
    _knownBundle = knownBundle;
    return this;
  }

}
