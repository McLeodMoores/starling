/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class DiscountingMethodCurveBuilder {
  protected static final ParSpreadMarketQuoteDiscountingCalculator CALCULATOR =
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  protected static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator SENSITIVITY_CALCULATOR =
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  //TODO fixing ts, known data should be passed into the build method
  //TODO market data should be passed into the build method - painful now because constructing attributes is annoying
  protected static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  protected final double _absoluteTolerance = 1e-10;
  protected final double _relativeTolerance = 1e-10;
  protected final int _maxSteps = 100;
  protected final List<String[]> _curveNames;
  protected final LinkedHashMap<String, Currency> _discountingCurves;
  protected final LinkedHashMap<String, IborIndex[]> _iborCurves;
  protected final LinkedHashMap<String, IndexON[]> _overnightCurves;
  protected final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> _nodes;
  protected final Map<String, Interpolator1D> _interpolatorForCurve;
  protected final MulticurveProviderDiscount _knownData;
  protected final Map<Index, ZonedDateTimeDoubleTimeSeries> _fixingTs;
  protected final MulticurveDiscountBuildingRepository _curveBuildingRepository;
  protected final Map<ZonedDateTime, MultiCurveBundle[]> _cached;

  protected DiscountingMethodCurveBuilder(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves, final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes,
      final Map<String, Interpolator1D> interpolatorForCurve, final MulticurveProviderDiscount knownData, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    _curveNames = curveNames;
    _discountingCurves = discountingCurves;
    _iborCurves = iborCurves;
    _overnightCurves = overnightCurves;
    _nodes = nodes;
    _interpolatorForCurve = interpolatorForCurve;
    _knownData = knownData;
    _fixingTs = fixingTs;
    _curveBuildingRepository = new MulticurveDiscountBuildingRepository(_absoluteTolerance, _relativeTolerance, _maxSteps);
    _cached = new HashMap<>();
  }

  public static CurveBuilderSetUp setUp() {
    return new CurveBuilderSetUp();
  }

  public DiscountingMethodCurveBuilder replaceMarketQuote(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketQuote) {
    final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = _nodes.get(curveName);
    if (nodesForCurve == null) {
      throw new IllegalStateException();
    }
    final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesWithReplacedPoint = new LinkedHashMap<>(nodesForCurve);
    final Double replacedPoint = nodesWithReplacedPoint.put(Pairs.of(instrumentGenerator, attributeGenerator), marketQuote);
    if (replacedPoint == null) {
      throw new IllegalStateException();
    }
    final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> newNodesForCurve = new HashMap<>(_nodes);
    newNodesForCurve.put(curveName, nodesWithReplacedPoint);
    return new DiscountingMethodCurveBuilder(new ArrayList<>(_curveNames), new LinkedHashMap<>(_discountingCurves), new LinkedHashMap<>(_iborCurves),
        new LinkedHashMap<>(_overnightCurves), newNodesForCurve, new HashMap<>(_interpolatorForCurve), _knownData.copy(), new HashMap<>(_fixingTs));
  }

  public Map<String, InstrumentDefinition<?>[]> getDefinitionsForCurves(final ZonedDateTime valuationDate) {
    _cached.clear();
    final Map<String, InstrumentDefinition<?>[]> definitionsForCurves = new HashMap<>();
    for (int i = 0; i < _curveNames.size(); i++) {
      final String[] curveNamesForUnit = _curveNames.get(i);
      for (final String curveNameForUnit : curveNamesForUnit) {
        final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodes = _nodes.get(curveNameForUnit);
        final Iterator<Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodesIterator = nodes.entrySet().iterator();
        final int nNodes = nodes.size();
        final InstrumentDefinition<?>[] definitions = new InstrumentDefinition[nNodes];
        for (int k = 0; k < nNodes; k++) {
          final Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double> info = nodesIterator.next();
          definitions[k] = info.getKey().getFirst().generateInstrument(valuationDate, info.getValue(), 1, info.getKey().getSecond());
        }
        definitionsForCurves.put(curveNameForUnit, definitions);
      }
    }
    return definitionsForCurves;
  }

  //TODO cache definitions on LocalDate
  public Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate) {
    MultiCurveBundle<GeneratorYDCurve>[] curveBundles = _cached.get(valuationDate);
    if (curveBundles == null) {
      curveBundles = new MultiCurveBundle[_curveNames.size()];
      for (int i = 0; i < _curveNames.size(); i++) {
        final String[] curveNamesForUnit = _curveNames.get(i);
        final SingleCurveBundle[] unitBundle = new SingleCurveBundle[curveNamesForUnit.length];
        for (int j = 0; j < curveNamesForUnit.length; j++) {
          final String curveName = curveNamesForUnit[j];
          final Interpolator1D interpolator = _interpolatorForCurve.get(curveName);
          final Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = _nodes.get(curveName);
          final Iterator<Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodesIterator = nodesForCurve.entrySet().iterator();
          final int nNodes = nodesForCurve.size();
          final InstrumentDerivative[] instruments = new InstrumentDerivative[nNodes];
          //TODO could do sorting of derivatives here
          final double[] curveInitialGuess = new double[nNodes];
          for (int k = 0; k < nNodes; k++) {
            final Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double> info = nodesIterator.next();
            final InstrumentDefinition<?> definition = info.getKey().getFirst().generateInstrument(valuationDate, info.getValue(), 1, info.getKey().getSecond());
            instruments[k] = CurveUtils.convert(definition, _fixingTs, valuationDate);
            curveInitialGuess[k] = definition.accept(CurveUtils.RATES_INITIALIZATION);
          }
          final GeneratorYDCurve instrumentGenerator = new GeneratorCurveYieldInterpolated(LastTimeCalculator.getInstance(), interpolator);
          unitBundle[j] = new SingleCurveBundle<GeneratorYDCurve>(curveName, instruments, curveInitialGuess, instrumentGenerator.finalGenerator(instruments));
        }
        curveBundles[i] = new MultiCurveBundle(unitBundle);
      }
    }
    _cached.put(valuationDate, curveBundles);
    return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, _knownData, _discountingCurves, _iborCurves, _overnightCurves, CALCULATOR,
        SENSITIVITY_CALCULATOR);
  }

//  public static class SetUp {
//    //TODO method that takes definitions
//    protected final List<String[]> _curveNames;
//    protected final LinkedHashMap<String, Currency> _discountingCurves;
//    protected final LinkedHashMap<String, IborIndex[]> _iborCurves;
//    protected final LinkedHashMap<String, IndexON[]> _overnightCurves;
//    protected final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> _nodes;
//    protected final Map<String, Interpolator1D> _interpolatorForCurve;
//    protected final Map<Index, ZonedDateTimeDoubleTimeSeries> _fixingTs;
//    protected MulticurveProviderDiscount _knownData = null;
//    protected String _nameForCurveType = null;
//    protected boolean _singleUnit = false;
//
//    protected SetUp() {
//      _curveNames = new ArrayList<>();
//      _discountingCurves = new LinkedHashMap<>();
//      _iborCurves = new LinkedHashMap<>();
//      _overnightCurves = new LinkedHashMap<>();
//      _interpolatorForCurve = new HashMap<>();
//      //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
//      _nodes = new LinkedHashMap<>();
//      _fixingTs = new HashMap<>();
//    }
//
//    protected SetUp(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
//        final LinkedHashMap<String, IndexON[]> overnightCurves, final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes,
//        final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final Map<String, Interpolator1D> interpolatorForCurve, final MulticurveProviderDiscount knownData,
//        final String nameForCurveType, final boolean singleUnit) {
//      _curveNames = new ArrayList<>(curveNames);
//      _discountingCurves = new LinkedHashMap<>(discountingCurves);
//      _iborCurves = new LinkedHashMap<>(iborCurves);
//      _overnightCurves = new LinkedHashMap<>(overnightCurves);
//      _interpolatorForCurve = new HashMap<>(interpolatorForCurve);
//      _nodes = new HashMap<>(nodes);
//      _fixingTs = new HashMap<>(fixingTs);
//      _knownData = knownData == null ? null : knownData.copy();
//      _nameForCurveType = nameForCurveType;
//      _singleUnit = singleUnit;
//    }
//
//    public SetUp building(final String... curveNames) {
//      if (_curveNames.isEmpty()) {
//        _curveNames.add(curveNames);
//        _singleUnit = true;
//        _nameForCurveType = null;
//        return this;
//      }
//      throw new IllegalStateException();
//    }
//
//    public SetUp buildingFirst(final String... curveNames) {
//      _curveNames.add(curveNames);
//      _singleUnit = false;
//      _nameForCurveType = null;
//      return this;
//    }
//
//    public SetUp thenBuilding(final String... curveNames) {
//      if (_curveNames.isEmpty() || _singleUnit) {
//        throw new IllegalStateException();
//      }
//      _curveNames.add(curveNames);
//      _nameForCurveType = null;
//      return this;
//    }
//
//    public SetUp withNode(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketData) {
//      Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = _nodes.get(curveName);
//      if (nodesForCurve == null) {
//        nodesForCurve = new LinkedHashMap<>();
//        _nodes.put(curveName, nodesForCurve);
//      }
//      nodesForCurve.put(Pairs.<GeneratorInstrument, GeneratorAttribute>of(instrumentGenerator, attributeGenerator), marketData);
//      //TODO if market data is already present, log then overwrite
//      return this;
//    }
//
//    //TODO add a withNode that takes definitions
//    //TODO the next 5 methods should be in a ConfigBuilder that extends this class - this should mean _nameForCurveType does not have to be null
//    public SetUp using(final String curveName) {
//      _nameForCurveType = curveName;
//      return this;
//    }
//
//    public SetUp forDiscounting(final Currency currency) {
//      if (_nameForCurveType == null) {
//        throw new IllegalStateException();
//      }
//      _discountingCurves.put(_nameForCurveType, currency);
//      return this;
//    }
//
//    public SetUp forIborIndex(final IborIndex... indices) {
//      if (_nameForCurveType == null) {
//        throw new IllegalStateException();
//      }
//      _iborCurves.put(_nameForCurveType, indices);
//      return this;
//    }
//
//    public SetUp forOvernightIndex(final IndexON... indices) {
//      if (_nameForCurveType == null) {
//        throw new IllegalStateException();
//      }
//      _overnightCurves.put(_nameForCurveType, indices);
//      return this;
//    }
//
//    public SetUp withInterpolator(final Interpolator1D interpolator) {
//      if (_nameForCurveType == null) {
//        throw new IllegalStateException();
//      }
//      _interpolatorForCurve.put(_nameForCurveType, interpolator);
//      return this;
//    }
//
//    //TODO move this
//    public SetUp withKnownData(final MulticurveProviderDiscount knownData) {
//      // probably better to merge this
//      _knownData = knownData;
//      _nameForCurveType = null;
//      return this;
//    }
//    //TODO move this
//    public SetUp withFixingTs(final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
//      _fixingTs.putAll(fixingTs);
//      _nameForCurveType = null;
//      return this;
//    }
//
//    public DiscountingMethodCurveBuilder getBuilder() {
//      return new DiscountingMethodCurveBuilder(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _interpolatorForCurve, _knownData,
//          _fixingTs);
//    }
//
//    public SetUp copy() {
//      return new SetUp(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _fixingTs, _interpolatorForCurve, _knownData, _nameForCurveType, _singleUnit);
//    }
//  }
}
