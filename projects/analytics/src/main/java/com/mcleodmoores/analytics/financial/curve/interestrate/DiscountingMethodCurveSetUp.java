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
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class DiscountingMethodCurveSetUp implements CurveSetUpInterface<MulticurveProviderDiscount> {
  //TODO method that takes definitions
  protected final List<String[]> _curveNames;
  //TODO should these live in curve type setup?
  protected final LinkedHashMap<String, Currency> _discountingCurves;
  protected final LinkedHashMap<String, IborTypeIndex[]> _iborCurves;
  protected final LinkedHashMap<String, OvernightIndex[]> _overnightCurves;
  protected final Map<String, DiscountingMethodCurveTypeSetUp> _curveTypes;
  protected final Map<String, List<InstrumentDefinition<?>>> _nodes;
  protected MulticurveProviderDiscount _knownData;
  protected CurveBuildingBlockBundle _knownBundle;

  protected DiscountingMethodCurveSetUp() {
    _curveNames = new ArrayList<>();
    _discountingCurves = new LinkedHashMap<>();
    _iborCurves = new LinkedHashMap<>();
    _overnightCurves = new LinkedHashMap<>();
    _curveTypes = new HashMap<>();
    _nodes = new LinkedHashMap<>();
    _knownData = null;
    _knownBundle = null;
  }

  protected DiscountingMethodCurveSetUp(final DiscountingMethodCurveSetUp setup) {
    //TODO copy
    _curveNames = setup._curveNames;
    _discountingCurves = setup._discountingCurves;
    _iborCurves = setup._iborCurves;
    _overnightCurves = setup._overnightCurves;
    _curveTypes = setup._curveTypes;
    // TODO sort
    _nodes = setup._nodes;
    _knownData = setup._knownData;
    _knownBundle = setup._knownBundle;
  }

  protected DiscountingMethodCurveSetUp(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborTypeIndex[]> iborCurves,
      final LinkedHashMap<String, OvernightIndex[]> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> newNodes,
      final Map<String, DiscountingMethodCurveTypeSetUp> curveTypes,  final MulticurveProviderDiscount knownData,
      final CurveBuildingBlockBundle knownBundle) {
    _curveNames = new ArrayList<>(curveNames);
    _discountingCurves = new LinkedHashMap<>(discountingCurves);
    _iborCurves = new LinkedHashMap<>(iborCurves);
    _overnightCurves = new LinkedHashMap<>(overnightCurves);
    _nodes = new HashMap<>(newNodes);
    _curveTypes = new HashMap<>(curveTypes);
    _knownData = knownData == null ? null : knownData.copy();
    _knownBundle = knownBundle == null ? null : knownBundle; //TODO no copy
  }


  @Override
  public DiscountingMethodCurveBuilder getBuilder() {
    return new DiscountingMethodCurveBuilder(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _curveTypes,
        _knownData, _knownBundle);
  }

  @Override
  public DiscountingMethodCurveSetUp copy() {
    // TODO not a copy
    return new DiscountingMethodCurveSetUp(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _curveTypes,
        _knownData, _knownBundle);
  }

  @Override
  public DiscountingMethodCurveSetUp building(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(curveNames);
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public DiscountingMethodCurveSetUp buildingFirst(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(curveNames);
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public DiscountingMethodCurveSetUp thenBuilding(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      throw new IllegalStateException();
    }
    _curveNames.add(curveNames);
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp using(final String curveName) {
    final DiscountingMethodCurveTypeSetUp type = new DiscountingMethodCurveTypeSetUp(curveName, this);
    final Object replaced = _curveTypes.put(curveName, type);
    if (replaced != null) {
      throw new IllegalStateException();
    }
    return type;
  }

  @Override
  public DiscountingMethodCurveSetUp addNode(final String curveName, final InstrumentDefinition<?> definition) {
    List<InstrumentDefinition<?>> nodesForCurve = _nodes.get(curveName);
    if (nodesForCurve == null) {
      nodesForCurve = new ArrayList<>();
      _nodes.put(curveName, nodesForCurve);
    }
    nodesForCurve.add(definition);
    return this;
  }

  @Override
  public DiscountingMethodCurveSetUp removeNodes(final String curveName) {
    _nodes.put(curveName, null);
    return this;
  }
  //TODO add a withNode that takes definitions

  @Override
  public DiscountingMethodCurveSetUp withKnownData(final MulticurveProviderDiscount knownData) {
    // probably better to merge this
    _knownData = knownData;
    return this;
  }

  @Override
  public DiscountingMethodCurveSetUp withKnownBundle(final CurveBuildingBlockBundle knownBundle) {
    _knownBundle = knownBundle;
    return this;
  }

}
