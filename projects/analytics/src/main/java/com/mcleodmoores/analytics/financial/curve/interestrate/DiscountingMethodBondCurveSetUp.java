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
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class DiscountingMethodBondCurveSetUp implements BondCurveSetUpInterface {
  private final List<List<String>> _curveNames;
  private final Map<String, DiscountingMethodBondCurveTypeSetUp> _curveTypes;
  private final Map<DiscountingMethodPreConstructedBondCurveTypeSetUp, YieldAndDiscountCurve> _preConstructedCurves;
  private final Map<String, List<InstrumentDefinition<?>>> _nodes;
  private FXMatrix _fxMatrix;
  private CurveBuildingBlockBundle _knownBundle;
  private double _absoluteTolerance = 1e-12;
  private double _relativeTolerance = 1e-12;
  private int _maxSteps = 100;

  DiscountingMethodBondCurveSetUp() {
    _curveNames = new ArrayList<>();
    _curveTypes = new HashMap<>();
    _preConstructedCurves = new HashMap<>();
    _nodes = new LinkedHashMap<>();
    _fxMatrix = new FXMatrix();
    _knownBundle = null;
  }

  protected DiscountingMethodBondCurveSetUp(final DiscountingMethodBondCurveSetUp setup) {
    //TODO copy
    _curveNames = setup._curveNames;
    _curveTypes = setup._curveTypes;
    _preConstructedCurves = setup._preConstructedCurves;
    //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
    _nodes = setup._nodes;
    _fxMatrix = setup._fxMatrix;
    _knownBundle = setup._knownBundle;
    _absoluteTolerance = setup._absoluteTolerance;
    _relativeTolerance = setup._relativeTolerance;
    _maxSteps = setup._maxSteps;
  }

  protected DiscountingMethodBondCurveSetUp(final List<List<String>> curveNames,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, DiscountingMethodBondCurveTypeSetUp> curveTypes,
      final Map<DiscountingMethodPreConstructedBondCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
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
    _knownBundle = knownBundle == null ? null : knownBundle; //TODO no copy
    _absoluteTolerance = absoluteTolerance;
    _relativeTolerance = relativeTolerance;
    _maxSteps = maxSteps;

  }

  @Override
  public DiscountingMethodBondCurveBuilder getBuilder() {
    final List<Pair<String, UniqueIdentifiable>> discountingCurves = new ArrayList<>();
    final List<Pair<String, List<IborTypeIndex>>> iborCurves = new ArrayList<>();
    final List<Pair<String, List<OvernightIndex>>> overnightCurves = new ArrayList<>();
    final List<Pair<String, List<Pair<Object, LegalEntityFilter<LegalEntity>>>>> issuerCurves = new ArrayList<>();
    for (final Map.Entry<String, DiscountingMethodBondCurveTypeSetUp> entry : _curveTypes.entrySet()) {
      final String curveName = entry.getKey();
      final DiscountingMethodBondCurveTypeSetUp setUp = entry.getValue();
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
      final List<Pair<Object, LegalEntityFilter<LegalEntity>>> issuers = setUp.getIssuers();
      if (issuers != null) {
        issuerCurves.add(Pairs.of(curveName, issuers));
      }
    }
    final Map<Currency, YieldAndDiscountCurve> knownDiscountingCurves = new HashMap<>();
    final Map<IborIndex, YieldAndDiscountCurve> knownIborCurves = new HashMap<>();
    final Map<IndexON, YieldAndDiscountCurve> knownOvernightCurves = new HashMap<>();
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> knownIssuerCurves = new HashMap<>();
    for (final Map.Entry<DiscountingMethodPreConstructedBondCurveTypeSetUp, YieldAndDiscountCurve> entry : _preConstructedCurves.entrySet()) {
      final DiscountingMethodPreConstructedBondCurveTypeSetUp setUp = entry.getKey();
      final YieldAndDiscountCurve curve = entry.getValue();
      final UniqueIdentifiable discountingCurveId = setUp.getDiscountingCurveId();
      if (discountingCurveId != null) {
        knownDiscountingCurves.put((Currency) discountingCurveId, curve);
      }
      final List<IborTypeIndex> iborCurveIndices = setUp.getIborCurveIndices();
      if (iborCurveIndices != null) {
        for (final IborTypeIndex index : iborCurveIndices) {
          knownIborCurves.put(IndexConverter.toIborIndex(index), curve);
        }
      }
      final List<OvernightIndex> overnightCurveIndices = setUp.getOvernightCurveIndices();
      if (overnightCurveIndices != null) {
        for (final OvernightIndex index : overnightCurveIndices) {
          knownOvernightCurves.put(IndexConverter.toIndexOn(index), curve);
        }
      }
      final List<Pair<Object, LegalEntityFilter<LegalEntity>>> issuers = setUp.getIssuers();
      if (issuers != null) {
        for (final Pair<Object, LegalEntityFilter<LegalEntity>> issuer : issuers) {
          knownIssuerCurves.put(issuer, curve);
        }
      }
    }
    return new DiscountingMethodBondCurveBuilder(_curveNames, discountingCurves, iborCurves, overnightCurves, issuerCurves, _nodes, _curveTypes,
        _fxMatrix, _preConstructedCurves, _knownBundle, _absoluteTolerance, _relativeTolerance, _maxSteps);
  }

  @Override
  public DiscountingMethodBondCurveSetUp copy() {
    return new DiscountingMethodBondCurveSetUp(_curveNames, _nodes, _curveTypes, _preConstructedCurves, _fxMatrix, _knownBundle,
        _absoluteTolerance, _relativeTolerance, _maxSteps);
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
    final DiscountingMethodBondCurveTypeSetUp type = new DiscountingMethodBondCurveTypeSetUp(this);
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
    _fxMatrix = fxMatrix;
    return this;
  }

  @Override
  public DiscountingMethodBondCurveSetUp removeNodes(final String curveName) {
    _nodes.put(curveName, null);
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

  Map<String, DiscountingMethodBondCurveTypeSetUp> getCurveTypes() {
    return _curveTypes;
  }

  Map<String, List<InstrumentDefinition<?>>> getNodes() {
    return _nodes;
  }

  CurveBuildingBlockBundle getKnownBundle() {
    return _knownBundle;
  }

  @Override
  public DiscountingMethodPreConstructedBondCurveTypeSetUp using(final YieldAndDiscountCurve curve) {
    final DiscountingMethodPreConstructedBondCurveTypeSetUp type = new DiscountingMethodPreConstructedBondCurveTypeSetUp(this);
    final Object replaced = _preConstructedCurves.put(type, curve);
    if (replaced != null) {
      throw new IllegalStateException();
    }
    return type;
  }

  @Override
  public DiscountingMethodBondCurveSetUp rootFindingAbsoluteTolerance(final double tolerance) {
    _absoluteTolerance = tolerance;
    return this;
  }

  @Override
  public DiscountingMethodBondCurveSetUp rootFindingRelativeTolerance(final double tolerance) {
    _relativeTolerance = tolerance;
    return this;
  }

  @Override
  public DiscountingMethodBondCurveSetUp rootFindingMaximumSteps(final int maxSteps) {
    _maxSteps = maxSteps;
    return this;
  }

}
