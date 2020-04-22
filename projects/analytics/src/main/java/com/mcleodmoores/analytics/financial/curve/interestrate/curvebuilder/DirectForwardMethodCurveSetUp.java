/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A builder interface that describes the steps to build a set of curves using the direct forward rates.
 *
 * Each curve must have a curve type (e.g. *ibor index, overnight index, with an interpolation method) and a set of nodes defined.
 *
 * This builder also allows optional pre-constructed curves, FX information and can be used to set up the root-finding method.
 *
 * Example usage:
 *
 * <pre>
 * {
 *   final DirectForwardMethodCurveSetUp curveBuilder = DirectForwardMethodCurveBuilder.setUp()
 *       .building(curveName)
 *       .using(curveName).forDiscounting(Currency.USD).withInterpolator(INTERPOLATOR);
 *
 *   Tenor startTenor = Tenor.of(Period.ZERO);
 *   for (int i = 0; i < curveTenors.length; i++) {
 *     curveBuilder.addNode(curveName, curveInstrument);
 *   }
 * }
 * </pre>
 */
public class DirectForwardMethodCurveSetUp implements CurveSetUpInterface {
  private final List<List<String>> _curveNames;
  private final Map<String, DirectForwardMethodCurveTypeSetUp> _curveTypes;
  private final Map<DirectForwardMethodPreConstructedCurveTypeSetUp, YieldAndDiscountCurve> _preConstructedCurves;
  private final Map<String, List<InstrumentDefinition<?>>> _nodes;
  private FXMatrix _fxMatrix;
  private CurveBuildingBlockBundle _knownBundle;
  private final RootFinderSetUp _rootFinder;

  /**
   * Constructor that creates an empty builder.
   */
  DirectForwardMethodCurveSetUp() {
    _curveNames = new ArrayList<>();
    _curveTypes = new HashMap<>();
    _preConstructedCurves = new HashMap<>();
    _nodes = new LinkedHashMap<>();
    _fxMatrix = new FXMatrix();
    _knownBundle = null;
    _rootFinder = new RootFinderSetUp();
  }

  /**
   * Constructor that takes an existing builder. Note that this is not a copy constructor, i.e. any object references are shared.
   *
   * @param builder
   *          the builder, not null
   */
  DirectForwardMethodCurveSetUp(final DirectForwardMethodCurveSetUp builder) {
    ArgumentChecker.notNull(builder, "builder");
    _curveNames = builder._curveNames;
    _curveTypes = builder._curveTypes;
    _preConstructedCurves = builder._preConstructedCurves;
    _nodes = builder._nodes;
    _fxMatrix = builder._fxMatrix;
    _knownBundle = builder._knownBundle;
    _rootFinder = builder._rootFinder;
  }

  /**
   * Constructor that copies the supplied data into new objects.
   *
   * @param curveNames
   *          the curve names, can be null
   * @param nodes
   *          the nodes for each curve, can be null
   * @param curveTypes
   *          the types for each curve, can be null
   * @param preConstructedCurves
   *          any pre-constructed curves, can be null
   * @param fxMatrix
   *          the FX rates, can be null
   * @param knownBundle
   *          any known sensitivities, can be null
   * @param rootFinder
   *          the root finder, not null
   */
  private DirectForwardMethodCurveSetUp(final List<List<String>> curveNames,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, DirectForwardMethodCurveTypeSetUp> curveTypes,
      final Map<DirectForwardMethodPreConstructedCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
      final FXMatrix fxMatrix,
      final CurveBuildingBlockBundle knownBundle,
      final RootFinderSetUp rootFinder) {
    _curveNames = curveNames == null ? new ArrayList<>() : new ArrayList<>(curveNames);
    _nodes = nodes == null ? new HashMap<>() : new HashMap<>(nodes);
    _curveTypes = curveTypes == null ? new HashMap<>() : new HashMap<>(curveTypes);
    _preConstructedCurves = preConstructedCurves == null ? new HashMap<>() : new HashMap<>(preConstructedCurves);
    _fxMatrix = fxMatrix == null ? new FXMatrix() : new FXMatrix(fxMatrix);
    if (knownBundle == null) {
      _knownBundle = null;
    } else {
      _knownBundle = new CurveBuildingBlockBundle();
      _knownBundle.addAll(knownBundle);
    }
    _rootFinder = rootFinder == null ? new RootFinderSetUp() : rootFinder;
  }

  @Override
  public DirectForwardMethodCurveBuilder getBuilder() {
    if (_curveNames.isEmpty()) {
      throw new IllegalStateException("Have not configured any curves");
    }
    final List<String> names = _curveNames.parallelStream().flatMap(e -> e.parallelStream()).collect(Collectors.toList());
    if (names.size() != _nodes.size()) {
      if (names.size() > _nodes.size()) {
        throw new IllegalStateException(
            "Have not added nodes for " + names.stream().filter(e -> !_nodes.keySet().contains(e)).collect(Collectors.toList()));
      }
      throw new IllegalStateException(
          "Have added nodes for " + _nodes.keySet().stream().filter(e -> !names.contains(e)).collect(Collectors.toList())
              + " but they have not been configured");
    }
    if (names.size() != _curveTypes.size()) {
      if (names.size() > _curveTypes.size()) {
        throw new IllegalStateException(
            "Have not added curve types for " + names.stream().filter(e -> !_curveTypes.keySet().contains(e)).collect(Collectors.toList()));
      }
      throw new IllegalStateException(
          "Have added curve types for " + _curveTypes.keySet().stream().filter(e -> !names.contains(e)).collect(Collectors.toList())
              + " but they have not been configured");
    }
    final List<Pair<String, UniqueIdentifiable>> discountingCurves = new ArrayList<>();
    final List<Pair<String, List<IborTypeIndex>>> iborCurves = new ArrayList<>();
    final List<Pair<String, List<OvernightIndex>>> overnightCurves = new ArrayList<>();
    _curveTypes.entrySet().forEach(e -> {
      final String curveName = e.getKey();
      final DirectForwardMethodCurveTypeSetUp setUp = e.getValue();
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
    });
    return new DirectForwardMethodCurveBuilder(_curveNames, discountingCurves, iborCurves, overnightCurves, _nodes, _curveTypes,
        _fxMatrix, _preConstructedCurves, _knownBundle, _rootFinder);
  }

  @Override
  public DirectForwardMethodCurveSetUp copy() {
    return new DirectForwardMethodCurveSetUp(_curveNames, _nodes, _curveTypes, _preConstructedCurves, _fxMatrix, _knownBundle,
        _rootFinder);
  }

  @Override
  public DirectForwardMethodCurveSetUp building(final String... curveNames) {
    ArgumentChecker.notEmpty(curveNames, "curveNames");
    if (_curveNames.isEmpty()) {
      _curveNames.add(new ArrayList<>(Arrays.asList(curveNames)));
      return this;
    }
    throw new IllegalStateException("Have already set curves to construct");
  }

  @Override
  public DirectForwardMethodCurveSetUp buildingFirst(final String... curveNames) {
    ArgumentChecker.notEmpty(curveNames, "curveNames");
    if (_curveNames.isEmpty()) {
      _curveNames.add(new ArrayList<>(Arrays.asList(curveNames)));
      return this;
    }
    throw new IllegalStateException("Have already set curves to construct");
  }

  @Override
  public DirectForwardMethodCurveSetUp thenBuilding(final String... curveNames) {
    ArgumentChecker.notEmpty(curveNames, "curveNames");
    if (_curveNames.isEmpty()) {
      throw new IllegalStateException("Have not set the first curves to construct");
    }
    _curveNames.add(new ArrayList<>(Arrays.asList(curveNames)));
    return this;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp using(final String curveName) {
    ArgumentChecker.notNull(curveName, "curveName");
    final DirectForwardMethodCurveTypeSetUp type = new DirectForwardMethodCurveTypeSetUp(this);
    final Object replaced = _curveTypes.put(curveName, type);
    if (replaced != null) {
      throw new IllegalStateException("Have already set up a configuration for a curve called " + curveName);
    }
    return type;
  }

  @Override
  public DirectForwardMethodCurveSetUp addNode(final String curveName, final InstrumentDefinition<?> definition) {
    ArgumentChecker.notNull(curveName, "curveName");
    ArgumentChecker.notNull(definition, "definition");
    List<InstrumentDefinition<?>> nodesForCurve = _nodes.get(curveName);
    if (nodesForCurve == null) {
      nodesForCurve = new ArrayList<>();
      _nodes.put(curveName, nodesForCurve);
    }
    nodesForCurve.add(definition);
    return this;
  }

  @Override
  public DirectForwardMethodCurveSetUp addFxMatrix(final FXMatrix fxMatrix) {
    _fxMatrix = ArgumentChecker.notNull(fxMatrix, "fxMatrix");
    return this;
  }

  @Override
  public DirectForwardMethodCurveSetUp removeNodes(final String curveName) {
    ArgumentChecker.notNull(curveName, "curveName");
    _nodes.put(curveName, null);
    return this;
  }

  @Override
  public DirectForwardMethodCurveSetUp removeCurve(final String curveName) {
    ArgumentChecker.notNull(curveName, "curveName");
    _curveNames.forEach(e -> e.remove(curveName));
    _nodes.remove(curveName);
    _curveTypes.remove(curveName);
    return this;
  }

  @Override
  public DirectForwardMethodCurveSetUp withKnownBundle(final CurveBuildingBlockBundle knownBundle) {
    _knownBundle = knownBundle;
    return this;
  }

  @Override
  public DirectForwardMethodPreConstructedCurveTypeSetUp using(final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(curve, "curve");
    final DirectForwardMethodPreConstructedCurveTypeSetUp type = new DirectForwardMethodPreConstructedCurveTypeSetUp(this);
    _preConstructedCurves.put(type, curve);
    return type;
  }

  @Override
  public DirectForwardMethodCurveSetUp rootFindingAbsoluteTolerance(final double tolerance) {
    _rootFinder.setAbsoluteTolerance(tolerance);
    return this;
  }

  @Override
  public DirectForwardMethodCurveSetUp rootFindingRelativeTolerance(final double tolerance) {
    _rootFinder.setRelativeTolerance(tolerance);
    return this;
  }

  @Override
  public DirectForwardMethodCurveSetUp rootFindingMaximumSteps(final int maxSteps) {
    _rootFinder.setMaxSteps(maxSteps);
    return this;
  }

  @Override
  public DirectForwardMethodCurveSetUp rootFindingMethodName(final String methodName) {
    _rootFinder.setRootFinderName(methodName);
    return this;
  }
}
