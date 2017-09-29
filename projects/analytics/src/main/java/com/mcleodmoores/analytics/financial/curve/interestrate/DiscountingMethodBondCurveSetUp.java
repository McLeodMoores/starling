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

import com.google.common.collect.LinkedListMultimap;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class DiscountingMethodBondCurveSetUp implements BondCurveSetUpInterface<IssuerProviderDiscount> {
  private final List<List<String>> _curveNames;
  //TODO should these live in curve type setup?
  private final List<Pair<String, UniqueIdentifiable>> _discountingCurves;
  private final List<Pair<String, List<IborTypeIndex>>> _iborCurves;
  private final List<Pair<String, List<OvernightIndex>>> _overnightCurves;
  private final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> _issuerCurves;
  private final Map<String, DiscountingMethodBondCurveTypeSetUp> _curveTypes;
  private final Map<String, List<InstrumentDefinition<?>>> _nodes;
  private FXMatrix _fxMatrix;
  private IssuerProviderDiscount _knownData;
  private CurveBuildingBlockBundle _knownBundle;

  protected DiscountingMethodBondCurveSetUp() {
    _curveNames = new ArrayList<>();
    _discountingCurves = new ArrayList<>();
    _iborCurves = new ArrayList<>();
    _overnightCurves = new ArrayList<>();
    _issuerCurves = LinkedListMultimap.create();
    _curveTypes = new HashMap<>();
    _nodes = new LinkedHashMap<>();
    _fxMatrix = new FXMatrix();
    _knownData = null;
    _knownBundle = null;
  }

  protected DiscountingMethodBondCurveSetUp(final DiscountingMethodBondCurveSetUp setup) {
    //TODO copy
    _curveNames = setup._curveNames;
    _discountingCurves = setup._discountingCurves;
    _iborCurves = setup._iborCurves;
    _overnightCurves = setup._overnightCurves;
    _issuerCurves = setup._issuerCurves;
    _curveTypes = setup._curveTypes;
    //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
    _nodes = setup._nodes;
    _fxMatrix = setup._fxMatrix;
    _knownData = setup._knownData;
    _knownBundle = setup._knownBundle;
  }

  protected DiscountingMethodBondCurveSetUp(final List<List<String>> curveNames, final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves, final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> issuerCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, DiscountingMethodBondCurveTypeSetUp> curveTypes,
      final FXMatrix fxMatrix,
      final IssuerProviderDiscount knownData,
      final CurveBuildingBlockBundle knownBundle) {
    _curveNames = new ArrayList<>(curveNames);
    _discountingCurves = new ArrayList<>(discountingCurves);
    _iborCurves = new ArrayList<>(iborCurves);
    _overnightCurves = new ArrayList<>(overnightCurves);
    _issuerCurves = LinkedListMultimap.create(issuerCurves);
    _nodes = new HashMap<>(nodes);
    _curveTypes = new HashMap<>(curveTypes);
    _fxMatrix = fxMatrix;
    _knownData = knownData == null ? null : knownData.copy();
    _knownBundle = knownBundle == null ? null : knownBundle; //TODO no copy
  }

  @Override
  public DiscountingMethodBondCurveBuilder getBuilder() {
    final IssuerProviderDiscount knownData;
    if (_knownData != null) {
      knownData = _knownData;
    } else {
      knownData = new IssuerProviderDiscount(_fxMatrix);
    }
    return new DiscountingMethodBondCurveBuilder(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _issuerCurves, _nodes, _curveTypes,
        knownData, _knownBundle);
  }

  @Override
  public DiscountingMethodBondCurveSetUp copy() {
    return new DiscountingMethodBondCurveSetUp(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _issuerCurves, _nodes, _curveTypes,
        _fxMatrix, _knownData, _knownBundle);
  }

  @Override
  public DiscountingMethodBondCurveSetUp building(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(Arrays.asList(curveNames));
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public DiscountingMethodBondCurveSetUp buildingFirst(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      _curveNames.add(Arrays.asList(curveNames));
      return this;
    }
    throw new IllegalStateException();
  }

  @Override
  public DiscountingMethodBondCurveSetUp thenBuilding(final String... curveNames) {
    if (_curveNames.isEmpty()) {
      throw new IllegalStateException();
    }
    _curveNames.add(Arrays.asList(curveNames));
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp using(final String curveName) {
    final DiscountingMethodBondCurveTypeSetUp type = new DiscountingMethodBondCurveTypeSetUp(curveName, this);
    final Object replaced = _curveTypes.put(curveName, type);
    if (replaced != null) {
      throw new IllegalStateException();
    }
    return type;
  }

  @Override
  public DiscountingMethodBondCurveSetUp addNode(final String curveName, final InstrumentDefinition<?> definition) {
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
  public DiscountingMethodBondCurveSetUp addFxMatrix(final FXMatrix fxMatrix) {
    if (_knownData != null) {
      throw new IllegalStateException();
    }
    _fxMatrix = fxMatrix;
    return this;
  }

  @Override
  public DiscountingMethodBondCurveSetUp removeNodes(final String curveName) {
    _nodes.put(curveName, null);
    return this;
  }

  @Override
  public DiscountingMethodBondCurveSetUp withKnownData(final IssuerProviderDiscount knownData) {
    if (_fxMatrix != null) {
      throw new IllegalStateException();
    }
    // probably better to merge this
    _knownData = knownData;
    return this;
  }

  @Override
  public DiscountingMethodBondCurveSetUp withKnownBundle(final CurveBuildingBlockBundle knownBundle) {
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

  LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuerCurves() {
    return _issuerCurves;
  }

  Map<String, DiscountingMethodBondCurveTypeSetUp> getCurveTypes() {
    return _curveTypes;
  }

  Map<String, List<InstrumentDefinition<?>>> getNodes() {
    return _nodes;
  }

  IssuerProviderDiscount getKnownData() {
    return _knownData;
  }

  CurveBuildingBlockBundle getKnownBundle() {
    return _knownBundle;
  }

}
