/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.builder;

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
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class CurveBuilderSetUp implements CurveSetUpInterface {
  //TODO method that takes definitions
  protected final List<String[]> _curveNames;
  protected final LinkedHashMap<String, Currency> _discountingCurves;
  protected final LinkedHashMap<String, IborIndex[]> _iborCurves;
  protected final LinkedHashMap<String, IndexON[]> _overnightCurves;
  protected final Map<String, CurveTypeSetUp> _types;
  protected final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> _nodes;
  protected final Map<String, Interpolator1D> _interpolatorForCurve;
  protected final Map<Index, ZonedDateTimeDoubleTimeSeries> _fixingTs;
  protected MulticurveProviderDiscount _knownData = null;

  protected CurveBuilderSetUp() {
    _curveNames = new ArrayList<>();
    _discountingCurves = new LinkedHashMap<>();
    _iborCurves = new LinkedHashMap<>();
    _overnightCurves = new LinkedHashMap<>();
    _interpolatorForCurve = new HashMap<>();
    _types = new HashMap<>();
    //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
    _nodes = new LinkedHashMap<>();
    _fixingTs = new HashMap<>();
  }

  protected CurveBuilderSetUp(final CurveBuilderSetUp setup) {
    //TODO copy
    _curveNames = setup._curveNames;
    _discountingCurves = setup._discountingCurves;
    _iborCurves = setup._iborCurves;
    _overnightCurves = setup._overnightCurves;
    _interpolatorForCurve = setup._interpolatorForCurve;
    _types = setup._types;
    //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
    _nodes = setup._nodes;
    _fixingTs = setup._fixingTs;
    _knownData = setup._knownData;
  }

  protected CurveBuilderSetUp(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves, final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final Map<String, Interpolator1D> interpolatorForCurve, final MulticurveProviderDiscount knownData) {
    _curveNames = new ArrayList<>(curveNames);
    _discountingCurves = new LinkedHashMap<>(discountingCurves);
    _iborCurves = new LinkedHashMap<>(iborCurves);
    _overnightCurves = new LinkedHashMap<>(overnightCurves);
    _interpolatorForCurve = new HashMap<>(interpolatorForCurve);
    _nodes = new HashMap<>(nodes);
    _types = new HashMap<>();
    _fixingTs = new HashMap<>(fixingTs);
    _knownData = knownData == null ? null : knownData.copy();
  }

  public CurveBuilderSetUp building(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(curveNames);
      return this;
    }
    throw new IllegalStateException();
  }

  public CurveBuilderSetUp buildingFirst(final String... curveNames) {
    _curveNames.add(curveNames);
    return this;
  }

  public CurveBuilderSetUp thenBuilding(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      throw new IllegalStateException();
    }
    _curveNames.add(curveNames);
    return this;
  }

  public CurveTypeSetUpInterface using(final String curveName) {
    final CurveTypeSetUp type = new CurveTypeSetUp(curveName, this);
    final CurveTypeSetUp replaced = _types.put(curveName, type);
    if (replaced != null) {
      throw new IllegalStateException();
    }
    return type;
  }

  public CurveBuilderSetUp withNode(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketData) {
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

  public DiscountingMethodCurveBuilder getBuilder() {
    return new DiscountingMethodCurveBuilder(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _interpolatorForCurve, _knownData,
        _fixingTs);
  }


  public CurveBuilderSetUp withKnownData(final MulticurveProviderDiscount knownData) {
    // probably better to merge this
    _knownData = knownData;
    return this;
  }

  public CurveBuilderSetUp withFixingTs(final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    _fixingTs.putAll(fixingTs);
    return this;
  }

  public CurveBuilderSetUp copy() {
    return new CurveBuilderSetUp(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _fixingTs, _interpolatorForCurve, _knownData);
  }
}