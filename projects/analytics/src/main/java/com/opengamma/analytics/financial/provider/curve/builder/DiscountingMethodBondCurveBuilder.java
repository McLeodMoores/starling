/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.builder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.LinkedListMultimap;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

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
  //TODO fixing ts, known data should be passed into the build method
  //TODO market data should be passed into the build method - painful now because constructing attributes is annoying
  //TODO bad hard-coding
  protected final double _absoluteTolerance = 1e-12;
  protected final double _relativeTolerance = 1e-12;
  protected final int _maxSteps = 100;

  public static DiscountingMethodBondCurveSetUp setUp() {
    return new DiscountingMethodBondCurveSetUp();
  }

  DiscountingMethodBondCurveBuilder(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves,
      final LinkedHashMap<String, IborIndex[]> iborCurves, final LinkedHashMap<String, IndexON[]> overnightCurves,
      final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> issuerCurves,
      final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes, final Map<String, ? extends CurveTypeSetUpInterface<IssuerProviderDiscount>> curveGenerators,
      final IssuerProviderDiscount knownData, final CurveBuildingBlockBundle knownBundle, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, curveGenerators, knownData, knownBundle, fixingTs);
    _issuerCurves = LinkedListMultimap.create(issuerCurves);
    _curveBuildingRepository = new IssuerDiscountBuildingRepository(_absoluteTolerance, _relativeTolerance, _maxSteps);
    _cached = new HashMap<>();
  }

  //TODO cache definitions on LocalDate
  @Override
  public Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate) {
    MultiCurveBundle<GeneratorYDCurve>[] curveBundles = _cached.get(valuationDate);
    if (curveBundles == null) {
      final Map<String, GeneratorYDCurve> generatorForCurve = new HashMap<>();
      curveBundles = new MultiCurveBundle[getCurveNames().size()];
      for (int i = 0; i < getCurveNames().size(); i++) {
        final String[] curveNamesForUnit = getCurveNames().get(i);
        final SingleCurveBundle[] unitBundle = new SingleCurveBundle[curveNamesForUnit.length];
        for (int j = 0; j < curveNamesForUnit.length; j++) {
          final String curveName = curveNamesForUnit[j];
          final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = getNodes().get(curveName);
          if (nodesForCurve == null) {
            throw new IllegalStateException();
          }
          final Iterator<Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodesIterator = nodesForCurve.entrySet().iterator();
          final int nNodes = nodesForCurve.size();
          final InstrumentDerivative[] instruments = new InstrumentDerivative[nNodes];
          //TODO could do sorting of derivatives here
          final double[] curveInitialGuess = new double[nNodes];
          for (int k = 0; k < nNodes; k++) {
            final Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double> info = nodesIterator.next();
            final InstrumentDefinition<?> definition = info.getKey().getFirst().generateInstrument(valuationDate, info.getValue(), 1, info.getKey().getSecond());
            instruments[k] = CurveUtils.convert(definition, getFixingTs(), valuationDate);
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
      final CurveBuildingBlockBundle knownBundle, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves) {
    throw new IllegalStateException();
  }

  Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> buildCurves(final MultiCurveBundle[] curveBundles, final IssuerProviderDiscount knownData,
      final CurveBuildingBlockBundle knownBundle, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves, final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> issuerCurves) {
    if (knownBundle != null) {
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, discountingCurves, iborCurves, overnightCurves, issuerCurves, CALCULATOR,
          SENSITIVITY_CALCULATOR);
    }
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, knownData, discountingCurves, iborCurves, overnightCurves, issuerCurves, CALCULATOR,
        SENSITIVITY_CALCULATOR);
  }

  @Override
  public CurveBuilder<IssuerProviderDiscount> replaceMarketQuote(final String curveName, final GeneratorInstrument instrumentGenerator,
      final GeneratorAttribute attributeGenerator, final double marketQuote) {
    final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = getNodes().get(curveName);
    if (nodesForCurve == null) {
      throw new IllegalStateException();
    }
    final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesWithReplacedPoint = new LinkedHashMap<>(nodesForCurve);
    final Double replacedPoint = nodesWithReplacedPoint.put(Pairs.of(instrumentGenerator, attributeGenerator), marketQuote);
    if (replacedPoint == null) {
      throw new IllegalStateException();
    }
    final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> newNodesForCurve = new HashMap<>(getNodes());
    newNodesForCurve.put(curveName, nodesWithReplacedPoint);
    return new DiscountingMethodBondCurveBuilder(getCurveNames(), getDiscountingCurves(), getIborCurves(), getOvernightCurves(), _issuerCurves,
        newNodesForCurve, getCurveGenerators(), getKnownData(), getKnownBundle(), getFixingTs());
  }

  @Override
  CurveBuilder<IssuerProviderDiscount> replaceMarketQuote(
      final List<String[]> curveNames,
      final LinkedHashMap<String, Currency> discountingCurves,
      final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves,
      final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> newNodesForCurve,
      final Map<String, ? extends CurveTypeSetUpInterface<IssuerProviderDiscount>> curveGenerators,
      final IssuerProviderDiscount knownData,
      final CurveBuildingBlockBundle knownBundle,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    throw new IllegalStateException();
//    return new DiscountingMethodBondCurveBuilder(curveNames, discountingCurves, iborCurves, overnightCurves, newNodesForCurve,
//        curveGenerators, knownData, knownBundle, fixingTs);
  }

}
