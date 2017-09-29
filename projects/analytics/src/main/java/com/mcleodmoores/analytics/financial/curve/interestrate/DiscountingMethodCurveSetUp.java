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
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class DiscountingMethodCurveSetUp implements CurveSetUpInterface<MulticurveProviderDiscount> {
  private final List<List<String>> _curveNames;
  private final Map<String, DiscountingMethodCurveTypeSetUp> _curveTypes;
  private final Map<String, List<InstrumentDefinition<?>>> _nodes;
  private FXMatrix _fxMatrix;
  private MulticurveProviderDiscount _knownData;
  private CurveBuildingBlockBundle _knownBundle;

  protected DiscountingMethodCurveSetUp() {
    _curveNames = new ArrayList<>();
    _curveTypes = new HashMap<>();
    _nodes = new LinkedHashMap<>();
    _knownData = null;
    _knownBundle = null;
    _fxMatrix = new FXMatrix();
  }

  protected DiscountingMethodCurveSetUp(final DiscountingMethodCurveSetUp setup) {
    //TODO copy
    _curveNames = setup._curveNames;
    _curveTypes = setup._curveTypes;
    // TODO sort
    _nodes = setup._nodes;
    _knownData = setup._knownData;
    _knownBundle = setup._knownBundle;
    _fxMatrix = setup._fxMatrix;
  }

  protected DiscountingMethodCurveSetUp(final List<List<String>> curveNames,
      final Map<String, List<InstrumentDefinition<?>>> newNodes,
      final Map<String, DiscountingMethodCurveTypeSetUp> curveTypes,
      final FXMatrix fxMatrix,
      final MulticurveProviderDiscount knownData, final CurveBuildingBlockBundle knownBundle) {
    _curveNames = new ArrayList<>(curveNames);
    _nodes = new HashMap<>(newNodes);
    _curveTypes = new HashMap<>(curveTypes);
    _fxMatrix = fxMatrix;
    _knownData = knownData == null ? null : knownData.copy();
    _knownBundle = knownBundle == null ? null : knownBundle; //TODO no copy
  }

  @Override
  public DiscountingMethodCurveBuilder getBuilder() {
    final List<Pair<String, UniqueIdentifiable>> discountingCurves = new ArrayList<>();
    final List<Pair<String, List<IborTypeIndex>>> iborCurves = new ArrayList<>();
    final List<Pair<String, List<OvernightIndex>>> overnightCurves = new ArrayList<>();
    for (final Map.Entry<String, DiscountingMethodCurveTypeSetUp> entry : _curveTypes.entrySet()) {
      final String curveName = entry.getKey();
      final DiscountingMethodCurveTypeSetUp setUp = entry.getValue();
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
    final MulticurveProviderDiscount knownData;
    if (_knownData != null) {
      knownData = _knownData;
    } else {
      knownData = new MulticurveProviderDiscount(_fxMatrix);
    }
    return new DiscountingMethodCurveBuilder(_curveNames, discountingCurves, iborCurves, overnightCurves, _nodes, _curveTypes, knownData, _knownBundle);
  }

  @Override
  public DiscountingMethodCurveSetUp copy() {
    // TODO not a copy
    return new DiscountingMethodCurveSetUp(_curveNames, _nodes, _curveTypes, _fxMatrix, _knownData, _knownBundle);
  }

  @Override
  public DiscountingMethodCurveSetUp building(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(Arrays.asList(curveNames));
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public DiscountingMethodCurveSetUp buildingFirst(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(Arrays.asList(curveNames));
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public DiscountingMethodCurveSetUp thenBuilding(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      throw new IllegalStateException();
    }
    _curveNames.add(Arrays.asList(curveNames));
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp using(final String curveName) {
    final DiscountingMethodCurveTypeSetUp type = new DiscountingMethodCurveTypeSetUp(this);
    final Object replaced = _curveTypes.put(curveName, type);
    if (replaced != null) {
      throw new IllegalStateException();
    }
    return type;
  }

  public DiscountingMethodPreConstructedCurveTypeSetUp using(final YieldAndDiscountCurve curve) {
    return null;
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
  public DiscountingMethodCurveSetUp addFxMatrix(final FXMatrix fxMatrix) {
    if (_knownData != null) {
      throw new IllegalStateException();
    }
    _fxMatrix = fxMatrix;
    return this;
  }

  @Override
  public DiscountingMethodCurveSetUp removeNodes(final String curveName) {
    _nodes.put(curveName, null);
    return this;
  }

  @Override
  public DiscountingMethodCurveSetUp withKnownData(final MulticurveProviderDiscount knownData) {
    if (_fxMatrix != null) {
      throw new IllegalStateException();
    }
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
