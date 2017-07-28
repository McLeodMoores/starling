/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class DiscountingMethodCurveSetUp implements CurveSetUpInterface<MulticurveProviderDiscount> {
  //TODO method that takes definitions
  protected final List<String[]> _curveNames;
  //TODO should these live in curve type setup?
  protected final LinkedHashMap<String, Currency> _discountingCurves;
  protected final LinkedHashMap<String, IborIndex[]> _iborCurves;
  protected final LinkedHashMap<String, IndexON[]> _overnightCurves;
  protected final Map<String, DiscountingMethodCurveTypeSetUp> _curveTypes;
  protected final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> _nodes;
  protected final Map<Index, ZonedDateTimeDoubleTimeSeries> _fixingTs;
  protected MulticurveProviderDiscount _knownData;
  protected CurveBuildingBlockBundle _knownBundle;

  protected DiscountingMethodCurveSetUp() {
    _curveNames = new ArrayList<>();
    _discountingCurves = new LinkedHashMap<>();
    _iborCurves = new LinkedHashMap<>();
    _overnightCurves = new LinkedHashMap<>();
    _curveTypes = new HashMap<>();
    //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
    _nodes = new LinkedHashMap<>();
    _fixingTs = new HashMap<>();
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
    //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
    _nodes = setup._nodes;
    _fixingTs = setup._fixingTs;
    _knownData = setup._knownData;
    _knownBundle = setup._knownBundle;
  }

  protected DiscountingMethodCurveSetUp(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves, final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final Map<String, DiscountingMethodCurveTypeSetUp> curveTypes,  final MulticurveProviderDiscount knownData,
      final CurveBuildingBlockBundle knownBundle) {
    _curveNames = new ArrayList<>(curveNames);
    _discountingCurves = new LinkedHashMap<>(discountingCurves);
    _iborCurves = new LinkedHashMap<>(iborCurves);
    _overnightCurves = new LinkedHashMap<>(overnightCurves);
    _nodes = new HashMap<>(nodes);
    _curveTypes = new HashMap<>(curveTypes);
    _fixingTs = new HashMap<>(fixingTs);
    _knownData = knownData == null ? null : knownData.copy();
    _knownBundle = knownBundle == null ? null : knownBundle; //TODO no copy
  }


  @Override
  public DiscountingMethodCurveBuilder getBuilder() {
    return new DiscountingMethodCurveBuilder(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _curveTypes,
        _knownData, _knownBundle, _fixingTs);
  }

  @Override
  public DiscountingMethodCurveSetUp copy() {
    return new DiscountingMethodCurveSetUp(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _fixingTs, _curveTypes,
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
  public DiscountingMethodCurveSetUp withNode(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketData) {
    Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = _nodes.get(curveName);
    if (nodesForCurve == null) {
      nodesForCurve = new LinkedHashMap<>();
      _nodes.put(curveName, nodesForCurve);
    }
    nodesForCurve.put(Pairs.<GeneratorInstrument, GeneratorAttribute>of(instrumentGenerator, attributeGenerator), marketData);
    //TODO if market data is already present, log then overwrite
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

  @Override
  public DiscountingMethodCurveSetUp withFixingTs(final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    _fixingTs.putAll(fixingTs);
    return this;
  }

}
