/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.LinkedListMultimap;
import com.mcleodmoores.analytics.financial.curve.CurveUtils;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.Index;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class DiscountingMethodBondCurveBuilder extends CurveBuilder<IssuerProviderDiscount> {
  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator CALCULATOR =
      ParSpreadMarketQuoteIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator SENSITIVITY_CALCULATOR =
      ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();
  private final IssuerDiscountBuildingRepository _curveBuildingRepository;
  private final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> _issuerCurves;
  private final Map<ZonedDateTime, MultiCurveBundle[]> _cached;
  //TODO bad hard-coding
  protected final double _absoluteTolerance = 1e-12;
  protected final double _relativeTolerance = 1e-12;
  protected final int _maxSteps = 100;

  public static DiscountingMethodBondCurveSetUp setUp() {
    return new DiscountingMethodBondCurveSetUp();
  }

  DiscountingMethodBondCurveBuilder(final List<List<String>> curveNames, final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves, final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final List<Pair<String, List<Pair<Object, LegalEntityFilter<LegalEntity>>>>> issuerCurves,
      final Map<String, List<InstrumentDefinition<?>>> nodes,
      final Map<String, ? extends CurveTypeSetUpInterface> curveGenerators,
      final IssuerProviderDiscount knownData, final CurveBuildingBlockBundle knownBundle) {
    super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, curveGenerators, knownData, knownBundle);
    _issuerCurves = LinkedListMultimap.create();
    for (final Pair<String, List<Pair<Object, LegalEntityFilter<LegalEntity>>>> issuerCurve : issuerCurves) {
      _issuerCurves.put(issuerCurve.getKey(), issuerCurve.getValue().get(0));
    }
    _curveBuildingRepository = new IssuerDiscountBuildingRepository(_absoluteTolerance, _relativeTolerance, _maxSteps);
    _cached = new HashMap<>();
  }

  @Override
  public Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixings) {
    MultiCurveBundle<GeneratorYDCurve>[] curveBundles = _cached.get(valuationDate);
    if (curveBundles == null) {
      final Map<String, GeneratorYDCurve> generatorForCurve = new HashMap<>();
      curveBundles = new MultiCurveBundle[getCurveNames().size()];
      for (int i = 0; i < getCurveNames().size(); i++) {
        final List<String> curveNamesForUnit = getCurveNames().get(i);
        final SingleCurveBundle[] unitBundle = new SingleCurveBundle[curveNamesForUnit.size()];
        for (int j = 0; j < curveNamesForUnit.size(); j++) {
          final String curveName = curveNamesForUnit.get(j);
          final List<InstrumentDefinition<?>> nodesForCurve = getNodes().get(curveName);
          if (nodesForCurve == null) {
            throw new IllegalStateException();
          }
          final int nNodes = nodesForCurve.size();
          final InstrumentDerivative[] instruments = new InstrumentDerivative[nNodes];
          //TODO could do sorting of derivatives here
          final double[] curveInitialGuess = new double[nNodes];
          for (int k = 0; k < nNodes; k++) {
            final InstrumentDefinition<?> definition = nodesForCurve.get(k);
            instruments[k] = CurveUtils.convert(definition, fixings, valuationDate);
            curveInitialGuess[k] = definition.accept(CurveUtils.RATES_INITIALIZATION);
          }
          final GeneratorYDCurve instrumentGenerator = getCurveGenerators().get(curveName).buildCurveGenerator(valuationDate).finalGenerator(instruments);
          generatorForCurve.put(curveName, instrumentGenerator);
          unitBundle[j] = new SingleCurveBundle<>(curveName, instruments, instrumentGenerator.initialGuess(curveInitialGuess), instrumentGenerator);
        }
        curveBundles[i] = new MultiCurveBundle<>(unitBundle);
      }
      _cached.put(valuationDate, curveBundles);
    }
    return buildCurves(curveBundles, getKnownData(), getKnownBundle(), getDiscountingCurves(), getIborCurves(), getOvernightCurves(), _issuerCurves);
  }

  @Override
  Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> buildCurves(final MultiCurveBundle[] curveBundles, final IssuerProviderDiscount knownData,
      final CurveBuildingBlockBundle knownBundle, final List<Pair<String, UniqueIdentifiable>> discountingCurves,
      final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves) {
    throw new IllegalStateException();
  }

  Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> buildCurves(final MultiCurveBundle[] curveBundles,
      final IssuerProviderDiscount knownData, final CurveBuildingBlockBundle knownBundle,
      final List<Pair<String, UniqueIdentifiable>> discountingCurves, final List<Pair<String, List<IborTypeIndex>>> iborCurves,
      final List<Pair<String, List<OvernightIndex>>> overnightCurves,
      final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> issuerCurves) {
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
    if (knownBundle != null) {
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, convertedDiscountingCurves,
          convertedIborCurves, convertedOvernightCurves, issuerCurves, CALCULATOR,
          SENSITIVITY_CALCULATOR);
    }
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, convertedDiscountingCurves,
        convertedIborCurves, convertedOvernightCurves, issuerCurves, CALCULATOR,
        SENSITIVITY_CALCULATOR);
  }

}
