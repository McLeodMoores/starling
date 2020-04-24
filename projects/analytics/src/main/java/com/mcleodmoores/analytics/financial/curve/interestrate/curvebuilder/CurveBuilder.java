/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.LinkedListMultimap;
import com.mcleodmoores.analytics.financial.curve.CurveUtils;
import com.mcleodmoores.analytics.financial.curve.CurveUtils.NodeOrderCalculator;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.Index;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * A base class for curve builders. These classes convert the information in {@link CurveSetUpInterface} and {@link CurveTypeSetUpInterface} into forms that the
 * classes that perform the calibration and sensitivity calculations.
 *
 * @param <T>
 *          the type of the curve bundle produces
 */
public abstract class CurveBuilder<T extends ParameterProviderInterface> {
  private final List<List<String>> _curveNames;
  private final List<Pair<String, UniqueIdentifiable>> _discountingCurves;
  private final List<Pair<String, List<IborTypeIndex>>> _iborCurves;
  private final List<Pair<String, List<OvernightIndex>>> _overnightCurves;
  private final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> _issuerCurves;
  private final Map<String, ? extends CurveTypeSetUpInterface> _curveTypes;
  private final FXMatrix _fxMatrix;
  private final Map<Currency, YieldAndDiscountCurve> _knownDiscountingCurves;
  private final Map<IborIndex, YieldAndDiscountCurve> _knownIborCurves;
  private final Map<IndexON, YieldAndDiscountCurve> _knownOvernightCurves;
  private final CurveBuildingBlockBundle _knownBundle;
  private final Map<String, List<InstrumentDefinition<?>>> _nodes;

  /**
   * Constructor.
   *
   * @param curveNames
   *          names of the curves to be constructed, not null
   * @param discountingCurves
   *          maps the curve name to a particular identifier that will use that curve for discounting, not null
   * @param iborCurves
   *          maps the curve name to ibor indices that will use that curve to calculate forward IBOR rates, not null
   * @param overnightCurves
   *          maps the curve name to overnight indices that will use that curve to calculate forward overnight rates, not null
   * @param nodes
   *          the nodes in each curve, not null
   * @param curveTypes
   *          the type of each curve, not null
   * @param fxMatrix
   *          any FX rates required to build the curves, can be null
   * @param preConstructedCurves
   *          pre-constructed curves, can be null
   * @param knownBundle
   *          sensitivity data for the pre-constructed curves, can be null
   */
  CurveBuilder(final List<List<String>> curveNames,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface> curveTypes,
      final FXMatrix fxMatrix,
      final Map<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
      final CurveBuildingBlockBundle knownBundle) {
    this(curveNames, discountingCurves, iborCurves, overnightCurves, null, nodes, curveTypes, fxMatrix, preConstructedCurves, knownBundle);
  }

  /**
   * Constructor.
   *
   * @param curveNames
   *          names of the curves to be constructed, not null
   * @param discountingCurves
   *          maps the curve name to a particular identifier that will use that curve for discounting, not null
   * @param iborCurves
   *          maps the curve name to ibor indices that will use that curve to calculate forward IBOR rates, not null
   * @param overnightCurves
   *          maps the curve name to overnight indices that will use that curve to calculate forward overnight rates, not null
   * @param issuerCurves
   *          maps the curve name to bond issuers that will use that curve to discount payments, can be null
   * @param nodes
   *          the nodes in each curve, not null
   * @param curveTypes
   *          the type of each curve, not null
   * @param fxMatrix
   *          any FX rates required to build the curves, can be null
   * @param preConstructedCurves
   *          pre-constructed curves, can be null
   * @param knownBundle
   *          sensitivity data for the pre-constructed curves, can be null
   */
  CurveBuilder(final List<List<String>> curveNames,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final List<Pair<String, List<Pair<Object, LegalEntityFilter<LegalEntity>>>>> issuerCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface> curveTypes,
      final FXMatrix fxMatrix,
      final Map<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
      final CurveBuildingBlockBundle knownBundle) {
    _curveNames = new ArrayList<>(ArgumentChecker.notEmpty(curveNames, "curveNames"));
    _discountingCurves = new ArrayList<>(ArgumentChecker.notNull(discountingCurves, "discountingCurves"));
    _iborCurves = new ArrayList<>(ArgumentChecker.notNull(iborCurves, "iborCurves"));
    _overnightCurves = new ArrayList<>(ArgumentChecker.notNull(overnightCurves, "overnightCurves"));
    _issuerCurves = LinkedListMultimap.create();
    if (issuerCurves != null) {
      for (final Pair<String, List<Pair<Object, LegalEntityFilter<LegalEntity>>>> issuerCurve : issuerCurves) {
        _issuerCurves.put(issuerCurve.getKey(), issuerCurve.getValue().get(0)); // TODO only one handled
      }
    }
    _nodes = new HashMap<>(ArgumentChecker.notEmpty(nodes, "nodes"));
    _curveTypes = new HashMap<>(ArgumentChecker.notEmpty(curveTypes, "curveTypes"));
    _fxMatrix = fxMatrix == null ? new FXMatrix() : fxMatrix;
    _knownDiscountingCurves = new HashMap<>();
    _knownIborCurves = new HashMap<>();
    _knownOvernightCurves = new HashMap<>();
    for (final Map.Entry<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> entry : preConstructedCurves.entrySet()) {
      final PreConstructedCurveTypeSetUp setUp = entry.getKey();
      final YieldAndDiscountCurve curve = entry.getValue();
      final UniqueIdentifiable discountingCurveId = setUp.getDiscountingCurveId();
      if (discountingCurveId != null) {
        _knownDiscountingCurves.put((Currency) discountingCurveId, curve); // TODO cast
      }
      final List<IborTypeIndex> iborCurveIndices = setUp.getIborCurveIndices();
      if (iborCurveIndices != null) {
        for (final IborTypeIndex index : iborCurveIndices) {
          _knownIborCurves.put(IndexConverter.toIborIndex(index), curve);
        }
      }
      final List<OvernightIndex> overnightCurveIndices = setUp.getOvernightCurveIndices();
      if (overnightCurveIndices != null) {
        for (final OvernightIndex index : overnightCurveIndices) {
          _knownOvernightCurves.put(IndexConverter.toIndexOn(index), curve);
        }
      }
    }
    if (knownBundle == null) {
      _knownBundle = null;
    } else {
      _knownBundle = new CurveBuildingBlockBundle();
      _knownBundle.addAll(knownBundle);
    }
  }

  /**
   * Builds the curves at a particular valuation date and time.
   *
   * @param valuationDate
   *          the valuation date, not null
   * @return the curves and sensitivities
   */
  public Pair<T, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate) {
    return buildCurves(valuationDate, Collections.emptyMap());
  }

  /**
   * Builds the curves at a particular valuation date and time, using the latest fixings.
   *
   * @param valuationDate
   *          the valuation date, not null
   * @param fixings
   *          the fixings, not null, can be empty
   * @return the curves and sensitivities
   */
  public Pair<T, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixings) {
    ArgumentChecker.notNull(valuationDate, "valuationDate");
    ArgumentChecker.notNull(fixings, "fixings");
    final Map<String, GeneratorYDCurve> generatorForCurve = new HashMap<>();
    final List<MultiCurveBundle<GeneratorYDCurve>> curveBundles = new ArrayList<>();
    for (final List<String> curveNamesForUnit : _curveNames) {
      final List<SingleCurveBundle<GeneratorYDCurve>> unitBundle = new ArrayList<>();
      for (final String curveName : curveNamesForUnit) {
        // TODO sensible behaviour if not set
        final NodeOrderCalculator nodeOrderCalculator = new CurveUtils.NodeOrderCalculator(_curveTypes.get(curveName).getNodeTimeCalculator());
        final List<InstrumentDefinition<?>> nodesForCurve = _nodes.get(curveName);
        if (nodesForCurve == null) {
          throw new IllegalStateException("No nodes found for curve called " + curveName);
        }
        final List<InstrumentDerivative> instruments = nodesForCurve.stream().map(e -> CurveUtils.convert(e, fixings, valuationDate))
            .sorted(nodeOrderCalculator).collect(Collectors.toList());
        final double[] curveInitialGuess = instruments.stream().mapToDouble(e -> e.accept(CurveUtils.RATES_INITIALIZATION)).toArray();
        final GeneratorYDCurve instrumentGenerator = _curveTypes.get(curveName).buildCurveGenerator(valuationDate)
            .finalGenerator(instruments.toArray(new InstrumentDerivative[0]));
        generatorForCurve.put(curveName, instrumentGenerator);
        unitBundle.add(new SingleCurveBundle<>(curveName, instruments.toArray(new InstrumentDerivative[0]), instrumentGenerator.initialGuess(curveInitialGuess),
            instrumentGenerator));
      }
      curveBundles.add(new MultiCurveBundle<>(unitBundle));
    }
    return buildCurves(curveBundles);
  }

  abstract Pair<T, CurveBuildingBlockBundle> buildCurves(List<MultiCurveBundle<GeneratorYDCurve>> curveBundles);

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

  Map<String, ? extends CurveTypeSetUpInterface> getCurveGenerators() {
    return _curveTypes;
  }

  /**
   * Gets the node instruments used to construct the curves.
   * 
   * @return the node instruments
   */
  public Map<String, List<InstrumentDefinition<?>>> getNodes() {
    return Collections.unmodifiableMap(_nodes);
  }

  Map<Currency, YieldAndDiscountCurve> getKnownDiscountingCurves() {
    return _knownDiscountingCurves;
  }

  Map<IborIndex, YieldAndDiscountCurve> getKnownIborCurves() {
    return _knownIborCurves;
  }

  Map<IndexON, YieldAndDiscountCurve> getKnownOvernightCurves() {
    return _knownOvernightCurves;
  }

  FXMatrix getFxMatrix() {
    return _fxMatrix;
  }

  CurveBuildingBlockBundle getKnownBundle() {
    return _knownBundle;
  }

  Map<String, ? extends CurveTypeSetUpInterface> getCurveTypes() {
    return _curveTypes;
  }
}
