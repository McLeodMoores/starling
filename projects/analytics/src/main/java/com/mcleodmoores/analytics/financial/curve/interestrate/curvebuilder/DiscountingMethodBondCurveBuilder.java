/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.CurveSetUpInterface.RootFinderSetUp;
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
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class DiscountingMethodBondCurveBuilder extends CurveBuilder<IssuerProviderDiscount> {
  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator CALCULATOR = ParSpreadMarketQuoteIssuerDiscountingCalculator
      .getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator SENSITIVITY_CALCULATOR = ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator
      .getInstance();
  private final IssuerDiscountBuildingRepository _curveBuildingRepository;
  private final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> _issuerCurves;
  private final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> _knownIssuerCurves;

  public static DiscountingMethodBondCurveSetUp setUp() {
    return new DiscountingMethodBondCurveSetUp();
  }

  DiscountingMethodBondCurveBuilder(
      final List<List<String>> curveNames,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final List<Pair<String, List<Pair<Object, LegalEntityFilter<LegalEntity>>>>> issuerCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface> curveTypes,
      final FXMatrix fxMatrix,
      final Map<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> preConstructedCurves,
      final CurveBuildingBlockBundle knownBundle,
      final RootFinderSetUp rootFinder) {
    super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, curveTypes, fxMatrix, preConstructedCurves, knownBundle);
    _issuerCurves = LinkedListMultimap.create();
    for (final Pair<String, List<Pair<Object, LegalEntityFilter<LegalEntity>>>> issuerCurve : issuerCurves) {
      _issuerCurves.put(issuerCurve.getKey(), issuerCurve.getValue().get(0));
    }
    _knownIssuerCurves = new HashMap<>();
    for (final Map.Entry<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> entry : preConstructedCurves.entrySet()) {
      // TODO
    }
    _curveBuildingRepository = new IssuerDiscountBuildingRepository(rootFinder.getAbsoluteTolerance(), rootFinder.getRelativeTolerance(),
        rootFinder.getMaxSteps(), rootFinder.getRootFinderName());
  }

  @Override
  public Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> buildCurves(
      final MultiCurveBundle[] curveBundles,
      final CurveBuildingBlockBundle knownBundle,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final FXMatrix fxMatrix,
      final Map<Currency, YieldAndDiscountCurve> knownDiscountingCurves,
      final Map<IborIndex, YieldAndDiscountCurve> knownIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> knownOvernightCurves) {
    final LinkedHashMap<String, Currency> convertedDiscountingCurves = new LinkedHashMap<>();
    for (final Pair<String, UniqueIdentifiable> entry : discountingCurves) {
      if (entry.getValue() instanceof Currency) {
        convertedDiscountingCurves.put(entry.getKey(), (Currency) entry.getValue());
      } else {
        throw new UnsupportedOperationException();
      }
    }
    final LinkedHashMap<String, IborIndex[]> convertedIborCurves = new LinkedHashMap<>();
    for (final Pair<String, List<IborTypeIndex>> entry : iborCurves) {
      final IborIndex[] converted = new IborIndex[entry.getValue().size()];
      int i = 0;
      for (final IborTypeIndex index : entry.getValue()) {
        converted[i++] = IndexConverter.toIborIndex(index);
      }
      convertedIborCurves.put(entry.getKey(), converted);
    }
    final LinkedHashMap<String, IndexON[]> convertedOvernightCurves = new LinkedHashMap<>();
    for (final Map.Entry<String, List<OvernightIndex>> entry : overnightCurves) {
      final IndexON[] converted = new IndexON[entry.getValue().size()];
      int i = 0;
      for (final OvernightIndex index : entry.getValue()) {
        converted[i++] = IndexConverter.toIndexOn(index);
      }
      convertedOvernightCurves.put(entry.getKey(), converted);
    }
    final IssuerProviderDiscount knownData = new IssuerProviderDiscount(knownDiscountingCurves, knownIborCurves, knownOvernightCurves,
        fxMatrix);
    if (knownBundle != null) {
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, convertedDiscountingCurves,
          convertedIborCurves, convertedOvernightCurves, _issuerCurves, CALCULATOR,
          SENSITIVITY_CALCULATOR);
    }
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, convertedDiscountingCurves,
        convertedIborCurves, convertedOvernightCurves, _issuerCurves, CALCULATOR,
        SENSITIVITY_CALCULATOR);
  }

}
