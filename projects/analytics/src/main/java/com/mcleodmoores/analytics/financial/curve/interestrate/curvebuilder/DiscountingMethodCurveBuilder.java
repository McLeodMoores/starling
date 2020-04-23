/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.CurveSetUpInterface.RootFinderSetUp;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * This class uses the information contained in {@link DiscountingMethodCurveSetUp} to construct curves using the discounting method.
 */
public class DiscountingMethodCurveBuilder extends CurveBuilder<MulticurveProviderDiscount> {
  private static final ParSpreadMarketQuoteDiscountingCalculator CALCULATOR = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator SENSITIVITY_CALCULATOR = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator
      .getInstance();
  private final MulticurveDiscountBuildingRepository _curveBuildingRepository;

  /**
   * Creates a builder that defines the curves.
   *
   * @return a set up object
   */
  public static DiscountingMethodCurveSetUp setUp() {
    return new DiscountingMethodCurveSetUp();
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
   * @param rootFinder
   *          the root-finder, not null
   */
  DiscountingMethodCurveBuilder(
      final List<List<String>> curveNames,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface> curveTypes,
      final FXMatrix fxMatrix,
      final Map<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
      final CurveBuildingBlockBundle knownBundle,
      final RootFinderSetUp rootFinder) {
    super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, curveTypes, fxMatrix, preConstructedCurves, knownBundle);
    ArgumentChecker.notNull(rootFinder, "rootFinder");
    _curveBuildingRepository = new MulticurveDiscountBuildingRepository(rootFinder.getAbsoluteTolerance(), rootFinder.getRelativeTolerance(),
        rootFinder.getMaxSteps(), rootFinder.getRootFinderName());
  }

  @Override
  Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> buildCurves(
      final List<MultiCurveBundle<GeneratorYDCurve>> curveBundles) {
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
    final MulticurveProviderDiscount knownData = new MulticurveProviderDiscount(getKnownDiscountingCurves(), getKnownIborCurves(),
        getKnownOvernightCurves(), getFxMatrix());
    if (getKnownBundle() != null) {
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles.toArray(new MultiCurveBundle[0]), knownData, getKnownBundle(),
          convertedDiscountingCurves, convertedIborCurves, convertedOvernightCurves, CALCULATOR, SENSITIVITY_CALCULATOR);
    }
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles.toArray(new MultiCurveBundle[0]), knownData, convertedDiscountingCurves,
        convertedIborCurves, convertedOvernightCurves, CALCULATOR, SENSITIVITY_CALCULATOR);
  }

}
