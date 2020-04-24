/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.CurveUtils;
import com.mcleodmoores.analytics.financial.curve.CurveUtils.NodeOrderCalculator;
import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.CurveSetUpInterface.RootFinderSetUp;
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
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.hullwhite.HullWhiteProviderDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * This class uses the information contained in {@link HullWhiteMethodCurveSetUp} to construct curves using a one-factor Hull-White model.
 */
public class HullWhiteMethodCurveBuilder extends CurveBuilder<HullWhiteOneFactorProviderDiscount> {
  private static final ParSpreadMarketQuoteHullWhiteCalculator CALCULATOR = ParSpreadMarketQuoteHullWhiteCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator SENSITIVITY_CALCULATOR = ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator
      .getInstance();
  private final HullWhiteProviderDiscountBuildingRepository _curveBuildingRepository;
  private final HullWhiteOneFactorPiecewiseConstantParameters _parameters;
  private final Currency _currency;
  private final RootFinderSetUp _rootFinder;

  /**
   * Creates a builder that defines the curves.
   *
   * @return a set up object
   */
  public static HullWhiteMethodCurveSetUp setUp() {
    return new HullWhiteMethodCurveSetUp();
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
   * @param parameters
   *          the Hull-White model parameters, not null
   * @param currency
   *          the Hull-White model currency, not null
   * @param rootFinder
   *          the root-finder, not null
   */
  HullWhiteMethodCurveBuilder(
      final List<List<String>> curveNames,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, HullWhiteMethodCurveTypeSetUp> curveTypes,
      final FXMatrix fxMatrix,
      final Map<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
      final CurveBuildingBlockBundle knownBundle,
      final HullWhiteOneFactorPiecewiseConstantParameters parameters,
      final Currency currency,
      final RootFinderSetUp rootFinder) {
    super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, curveTypes, fxMatrix, preConstructedCurves, knownBundle);
    _parameters = ArgumentChecker.notNull(parameters, "parameters");
    _currency = ArgumentChecker.notNull(currency, "currency");
    _rootFinder = ArgumentChecker.notNull(rootFinder, "rootFinder");
    _curveBuildingRepository = new HullWhiteProviderDiscountBuildingRepository(_rootFinder.getAbsoluteTolerance(), rootFinder.getRelativeTolerance(),
        rootFinder.getMaxSteps(), rootFinder.getRootFinderName());
  }

  @Override
  Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> buildCurves(final List<MultiCurveBundle<GeneratorYDCurve>> curveBundles) {
    ArgumentChecker.notNull(curveBundles, "curveBundles");
    if (getDiscountingCurves().stream().anyMatch(e -> !(e.getValue() instanceof Currency))) {
      throw new UnsupportedOperationException("Can only have Currency as the discounting curve id");
    }
    final LinkedHashMap<String, Currency> convertedDiscountingCurves = getDiscountingCurves().stream()
        .collect(Collectors.toMap(Pair::getFirst, e -> (Currency) e.getValue(), (e1, e2) -> e1, LinkedHashMap::new));
    final LinkedHashMap<String, IborIndex[]> convertedIborCurves = getIborCurves().stream()
        .collect(Collectors.toMap(
            Pair::getFirst,
            e1 -> e1.getValue().stream().map(e2 -> IndexConverter.toIborIndex(e2)).toArray(IborIndex[]::new),
            (e1, e2) -> e1,
            LinkedHashMap::new));
    final LinkedHashMap<String, IndexON[]> convertedOvernightCurves = getOvernightCurves().stream()
        .collect(Collectors.toMap(
            Pair::getFirst,
            e1 -> e1.getValue().stream().map(e2 -> IndexConverter.toIndexOn(e2)).toArray(IndexON[]::new),
            (e1, e2) -> e1,
            LinkedHashMap::new));
    final HullWhiteOneFactorProviderDiscount knownData = new HullWhiteOneFactorProviderDiscount(
        new MulticurveProviderDiscount(getKnownDiscountingCurves(), getKnownIborCurves(), getKnownOvernightCurves(), getFxMatrix()), _parameters, _currency);
    if (getKnownBundle() != null) {
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles.toArray(new MultiCurveBundle[0]), knownData, getKnownBundle(),
          convertedDiscountingCurves,
          convertedIborCurves, convertedOvernightCurves, CALCULATOR, SENSITIVITY_CALCULATOR);
    }
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles.toArray(new MultiCurveBundle[0]), knownData, convertedDiscountingCurves,
        convertedIborCurves, convertedOvernightCurves, CALCULATOR, SENSITIVITY_CALCULATOR);
  }

  // TODO should disappear - get correct builders etc by choosing a curve building type in buildCurves()
  public Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> buildCurvesWithoutConvexityAdjustment(
      final ZonedDateTime valuationDate,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixings) {
    final Map<String, GeneratorYDCurve> generatorForCurve = new HashMap<>();
    final int size = getCurveNames().size();
    final MultiCurveBundle[] curveBundles = new MultiCurveBundle[size];
    for (int i = 0; i < size; i++) {
      final List<String> curveNamesForUnit = getCurveNames().get(i);
      final SingleCurveBundle[] unitBundle = new SingleCurveBundle[curveNamesForUnit.size()];
      for (int j = 0; j < curveNamesForUnit.size(); j++) {
        final String curveName = curveNamesForUnit.get(j);
        // TODO sensible behaviour if not set
        final NodeOrderCalculator nodeOrderCalculator = new CurveUtils.NodeOrderCalculator(
            getCurveTypes().get(curveName).getNodeTimeCalculator());
        final List<InstrumentDefinition<?>> nodesForCurve = getNodes().get(curveName);
        if (nodesForCurve == null) {
          throw new IllegalStateException();
        }
        final int nNodes = nodesForCurve.size();
        final InstrumentDerivative[] instruments = new InstrumentDerivative[nNodes];
        final double[] curveInitialGuess = new double[nNodes];
        for (int k = 0; k < nNodes; k++) {
          final InstrumentDefinition<?> definition = nodesForCurve.get(k);
          instruments[k] = CurveUtils.convert(definition, fixings, valuationDate);
        }
        Arrays.sort(instruments, nodeOrderCalculator);
        for (int k = 0; k < nNodes; k++) {
          curveInitialGuess[k] = instruments[k].accept(CurveUtils.RATES_INITIALIZATION);
        }
        final GeneratorYDCurve instrumentGenerator = getCurveGenerators().get(curveName).buildCurveGenerator(valuationDate)
            .finalGenerator(instruments);
        generatorForCurve.put(curveName, instrumentGenerator);
        unitBundle[j] = new SingleCurveBundle<>(curveName, instruments, instrumentGenerator.initialGuess(curveInitialGuess),
            instrumentGenerator);
      }
      curveBundles[i] = new MultiCurveBundle<>(unitBundle);
    }
    final MulticurveDiscountBuildingRepository curveBuildingRepository = new MulticurveDiscountBuildingRepository(_rootFinder.getAbsoluteTolerance(),
        _rootFinder.getRelativeTolerance(), _rootFinder.getMaxSteps(), _rootFinder.getRootFinderName());
    final MulticurveProviderDiscount knownData = new MulticurveProviderDiscount(getKnownDiscountingCurves(), getKnownIborCurves(),
        getKnownOvernightCurves(), getFxMatrix());
    final LinkedHashMap<String, Currency> convertedDiscountingCurves = new LinkedHashMap<>();
    for (final Pair<String, UniqueIdentifiable> entry : getDiscountingCurves()) {
      if (entry.getValue() instanceof Currency) {
        convertedDiscountingCurves.put(entry.getKey(), (Currency) entry.getValue());
      } else {
        throw new UnsupportedOperationException();
      }
    }
    final LinkedHashMap<String, IborIndex[]> convertedIborCurves = new LinkedHashMap<>();
    for (final Pair<String, List<IborTypeIndex>> entry : getIborCurves()) {
      final IborIndex[] converted = new IborIndex[entry.getValue().size()];
      int i = 0;
      for (final IborTypeIndex index : entry.getValue()) {
        converted[i++] = IndexConverter.toIborIndex(index);
      }
      convertedIborCurves.put(entry.getKey(), converted);
    }
    final LinkedHashMap<String, IndexON[]> convertedOvernightCurves = new LinkedHashMap<>();
    for (final Pair<String, List<OvernightIndex>> entry : getOvernightCurves()) {
      final IndexON[] converted = new IndexON[entry.getValue().size()];
      int i = 0;
      for (final OvernightIndex index : entry.getValue()) {
        converted[i++] = IndexConverter.toIndexOn(index);
      }
      convertedOvernightCurves.put(entry.getKey(), converted);
    }
    if (getKnownBundle() != null) {
      return curveBuildingRepository.makeCurvesFromDerivatives(
          curveBundles, knownData, getKnownBundle(), convertedDiscountingCurves, convertedIborCurves, convertedOvernightCurves,
          ParSpreadMarketQuoteDiscountingCalculator.getInstance(), ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance());
    }
    return curveBuildingRepository.makeCurvesFromDerivatives(
        curveBundles, knownData, convertedDiscountingCurves, convertedIborCurves, convertedOvernightCurves,
        ParSpreadMarketQuoteDiscountingCalculator.getInstance(), ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance());
  }
}
