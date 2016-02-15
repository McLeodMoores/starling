/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveTestUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class DiscountingMethodCurveUtils {
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PAR_SPREAD_CALCULATOR =
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PAR_SPREAD_SENSITIVITY_CALCULATOR =
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;
  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY =
      new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  public static class DiscountingMethodCurveBuilder {
    static final ParSpreadMarketQuoteDiscountingCalculator CALCULATOR =
        ParSpreadMarketQuoteDiscountingCalculator.getInstance();
    static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator SENSITIVITY_CALCULATOR =
        ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
    //TODO fixing ts, known data should be passed into the build method
    //TODO market data should be passed into the build method - painful now because constructing attributes is annoying
    static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
    final double _absoluteTolerance = 1e-10;
    final double _relativeTolerance = 1e-10;
    final int _maxSteps = 100;
    final List<String[]> _curveNames;
    final LinkedHashMap<String, Currency> _discountingCurves;
    final LinkedHashMap<String, IborIndex[]> _iborCurves;
    final LinkedHashMap<String, IndexON[]> _overnightCurves;
    final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> _nodes;
    final Map<String, Interpolator1D> _interpolatorForCurve;
    final MulticurveProviderDiscount _knownData;
    final Map<Index, ZonedDateTimeDoubleTimeSeries> _fixingTs;
    final MulticurveDiscountBuildingRepository _curveBuildingRepository;
    final Map<ZonedDateTime, MultiCurveBundle[]> _cached;

    DiscountingMethodCurveBuilder(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
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

    public static ConfigBuilder setUp() {
      return new ConfigBuilder();
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
              instruments[k] = CurveTestUtils.convert(definition, _fixingTs, valuationDate);
              curveInitialGuess[k] = definition.accept(CurveTestUtils.RATES_INITIALIZATION);
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

    public static class ConfigBuilder {
      //TODO method that takes definitions
      final List<String[]> _curveNames;
      final LinkedHashMap<String, Currency> _discountingCurves;
      final LinkedHashMap<String, IborIndex[]> _iborCurves;
      final LinkedHashMap<String, IndexON[]> _overnightCurves;
      final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> _nodes;
      final Map<String, Interpolator1D> _interpolatorForCurve;
      final Map<Index, ZonedDateTimeDoubleTimeSeries> _fixingTs;
      MulticurveProviderDiscount _knownData = null;
      String _nameForCurveType = null;
      boolean _singleUnit = false;

      ConfigBuilder() {
        _curveNames = new ArrayList<>();
        _discountingCurves = new LinkedHashMap<>();
        _iborCurves = new LinkedHashMap<>();
        _overnightCurves = new LinkedHashMap<>();
        _interpolatorForCurve = new HashMap<>();
        //TODO currently have to add things in the right order for each curve - need to have comparator for attribute generator tenors
        _nodes = new LinkedHashMap<>();
        _fixingTs = new HashMap<>();
      }

      ConfigBuilder(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
          final LinkedHashMap<String, IndexON[]> overnightCurves, final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes,
          final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final Map<String, Interpolator1D> interpolatorForCurve, final MulticurveProviderDiscount knownData,
          final String nameForCurveType, final boolean singleUnit) {
        _curveNames = new ArrayList<>(curveNames);
        _discountingCurves = new LinkedHashMap<>(discountingCurves);
        _iborCurves = new LinkedHashMap<>(iborCurves);
        _overnightCurves = new LinkedHashMap<>(overnightCurves);
        _interpolatorForCurve = new HashMap<>(interpolatorForCurve);
        _nodes = new HashMap<>(nodes);
        _fixingTs = new HashMap<>(fixingTs);
        _knownData = knownData == null ? null : knownData.copy();
        _nameForCurveType = nameForCurveType;
        _singleUnit = singleUnit;
      }

      public ConfigBuilder building(final String... curveNames) {
        if (_curveNames.isEmpty()) {
          _curveNames.add(curveNames);
          _singleUnit = true;
          _nameForCurveType = null;
          return this;
        }
        throw new IllegalStateException();
      }

      public ConfigBuilder buildingFirst(final String... curveNames) {
        _curveNames.add(curveNames);
        _singleUnit = false;
        _nameForCurveType = null;
        return this;
      }

      public ConfigBuilder thenBuilding(final String... curveNames) {
        if (_curveNames.isEmpty() || _singleUnit) {
          throw new IllegalStateException();
        }
        _curveNames.add(curveNames);
        _nameForCurveType = null;
        return this;
      }

      public ConfigBuilder withNode(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketData) {
        Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = _nodes.get(curveName);
        if (nodesForCurve == null) {
          nodesForCurve = new LinkedHashMap<>();
          _nodes.put(curveName, nodesForCurve);
        }
        nodesForCurve.put(Pairs.<GeneratorInstrument, GeneratorAttribute>of(instrumentGenerator, attributeGenerator), marketData);
        //TODO if market data is already present, log then overwrite
        return this;
      }

      //TODO add a withNode that takes definitions
      //TODO the next 5 methods should be in a ConfigBuilder that extends this class - this should mean _nameForCurveType does not have to be null
      public ConfigBuilder using(final String curveName) {
        _nameForCurveType = curveName;
        return this;
      }

      public ConfigBuilder forDiscounting(final Currency currency) {
        if (_nameForCurveType == null) {
          throw new IllegalStateException();
        }
        _discountingCurves.put(_nameForCurveType, currency);
        return this;
      }

      public ConfigBuilder forIborIndex(final IborIndex... indices) {
        if (_nameForCurveType == null) {
          throw new IllegalStateException();
        }
        _iborCurves.put(_nameForCurveType, indices);
        return this;
      }

      public ConfigBuilder forOvernightIndex(final IndexON... indices) {
        if (_nameForCurveType == null) {
          throw new IllegalStateException();
        }
        _overnightCurves.put(_nameForCurveType, indices);
        return this;
      }

      public ConfigBuilder withInterpolator(final Interpolator1D interpolator) {
        if (_nameForCurveType == null) {
          throw new IllegalStateException();
        }
        _interpolatorForCurve.put(_nameForCurveType, interpolator);
        return this;
      }

      //TODO move this
      public ConfigBuilder withKnownData(final MulticurveProviderDiscount knownData) {
        // probably better to merge this
        _knownData = knownData;
        _nameForCurveType = null;
        return this;
      }
      //TODO move this
      public ConfigBuilder withFixingTs(final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
        _fixingTs.putAll(fixingTs);
        _nameForCurveType = null;
        return this;
      }

      public DiscountingMethodCurveBuilder getBuilder() {
        return new DiscountingMethodCurveBuilder(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _interpolatorForCurve, _knownData,
            _fixingTs);
      }

      public ConfigBuilder copy() {
        return new ConfigBuilder(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _fixingTs, _interpolatorForCurve, _knownData, _nameForCurveType, _singleUnit);
      }
    }
  }

  public static class DiscountingMethodCurveBuilder2 extends DiscountingMethodCurveBuilder {

    DiscountingMethodCurveBuilder2(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
        final LinkedHashMap<String, IndexON[]> overnightCurves, final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes,
        final Map<String, Interpolator1D> interpolatorForCurve, final MulticurveProviderDiscount knownData, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
      super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, interpolatorForCurve, knownData, fixingTs);
    }

    public static ConfigBuilder2 setUp() {
      return new ConfigBuilder2();
    }

    @Override
    public Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate) {
      throw new UnsupportedOperationException();
    }

    public Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> buildCurves(final ZonedDateTime valuationDate, final ZonedDateTime[] meetingDates) {
      final double[] meetingTimes = new double[meetingDates.length];
      for (int i = 0; i < meetingTimes.length; i++) {
        meetingTimes[i] = TimeCalculator.getTimeBetween(valuationDate, meetingDates[i]);
      }
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
            final List<Object> temp = new ArrayList<>();
            for (int k = 0; k < nNodes; k++) {
              final Map.Entry<Pair<GeneratorInstrument, GeneratorAttribute>, Double> info = nodesIterator.next();
              final InstrumentDefinition<?> definition = info.getKey().getFirst().generateInstrument(valuationDate, info.getValue(), 1, info.getKey().getSecond());
              temp.add(definition);
              instruments[k] = CurveTestUtils.convert(definition, _fixingTs, valuationDate);
              curveInitialGuess[k] = definition.accept(CurveTestUtils.RATES_INITIALIZATION);
            }
            if (curveName.equals("EUR Dsc")) {
              final GeneratorYDCurve instrumentGenerator = new GeneratorCurveDiscountFactorInterpolatedNode(meetingTimes, interpolator);
              unitBundle[j] = new SingleCurveBundle<GeneratorYDCurve>(curveName, instruments, curveInitialGuess, instrumentGenerator.finalGenerator(instruments));
            } else {
              final GeneratorYDCurve instrumentGenerator = new GeneratorCurveYieldInterpolated(LastTimeCalculator.getInstance(), interpolator);
              unitBundle[j] = new SingleCurveBundle<GeneratorYDCurve>(curveName, instruments, curveInitialGuess, instrumentGenerator.finalGenerator(instruments));
            }
          }
          curveBundles[i] = new MultiCurveBundle(unitBundle);
        }
      }
      _cached.put(valuationDate, curveBundles);
      return _curveBuildingRepository.makeCurvesFromDerivatives(curveBundles, _knownData, _discountingCurves, _iborCurves, _overnightCurves, CALCULATOR,
          SENSITIVITY_CALCULATOR);
    }

    public static class ConfigBuilder2 extends ConfigBuilder {

      ConfigBuilder2() {
        super();
      }

      ConfigBuilder2(final List<String[]> curveNames, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
          final LinkedHashMap<String, IndexON[]> overnightCurves, final Map<String, Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double>> nodes,
          final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final Map<String, Interpolator1D> interpolatorForCurve, final MulticurveProviderDiscount knownData,
          final String nameForCurveType, final boolean singleUnit) {
        super(curveNames, discountingCurves, iborCurves, overnightCurves, nodes, fixingTs, interpolatorForCurve, knownData, nameForCurveType, singleUnit);
      }

      @Override
      public ConfigBuilder2 building(final String... curveNames) {
        if (_curveNames.isEmpty()) {
          _curveNames.add(curveNames);
          _singleUnit = true;
          _nameForCurveType = null;
          return this;
        }
        throw new IllegalStateException();
      }

      @Override
      public ConfigBuilder2 buildingFirst(final String... curveNames) {
        _curveNames.add(curveNames);
        _singleUnit = false;
        _nameForCurveType = null;
        return this;
      }

      @Override
      public ConfigBuilder2 thenBuilding(final String... curveNames) {
        if (_curveNames.isEmpty() || _singleUnit) {
          throw new IllegalStateException();
        }
        _curveNames.add(curveNames);
        _nameForCurveType = null;
        return this;
      }

      @Override
      public ConfigBuilder2 withNode(final String curveName, final GeneratorInstrument instrumentGenerator, final GeneratorAttribute attributeGenerator, final double marketData) {
        Map<Pair<GeneratorInstrument, GeneratorAttribute>, Double> nodesForCurve = _nodes.get(curveName);
        if (nodesForCurve == null) {
          nodesForCurve = new LinkedHashMap<>();
          _nodes.put(curveName, nodesForCurve);
        }
        nodesForCurve.put(Pairs.<GeneratorInstrument, GeneratorAttribute>of(instrumentGenerator, attributeGenerator), marketData);
        //TODO if market data is already present, log then overwrite
        return this;
      }

      //TODO add a withNode that takes definitions
      @Override
      public ConfigBuilder2 using(final String curveName) {
        _nameForCurveType = curveName;
        return this;
      }

      @Override
      public ConfigBuilder2 forDiscounting(final Currency currency) {
        if (_nameForCurveType == null) {
          throw new IllegalStateException();
        }
        _discountingCurves.put(_nameForCurveType, currency);
        return this;
      }

      @Override
      public ConfigBuilder2 forIborIndex(final IborIndex... indices) {
        if (_nameForCurveType == null) {
          throw new IllegalStateException();
        }
        _iborCurves.put(_nameForCurveType, indices);
        return this;
      }

      @Override
      public ConfigBuilder2 forOvernightIndex(final IndexON... indices) {
        if (_nameForCurveType == null) {
          throw new IllegalStateException();
        }
        _overnightCurves.put(_nameForCurveType, indices);
        return this;
      }

      @Override
      public ConfigBuilder2 withInterpolator(final Interpolator1D interpolator) {
        if (_nameForCurveType == null) {
          throw new IllegalStateException();
        }
        _interpolatorForCurve.put(_nameForCurveType, interpolator);
        return this;
      }

      //TODO move this
      @Override
      public ConfigBuilder2 withKnownData(final MulticurveProviderDiscount knownData) {
        // probably better to merge this
        _knownData = knownData;
        _nameForCurveType = null;
        return this;
      }
      //TODO move this
      @Override
      public ConfigBuilder2 withFixingTs(final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
        _fixingTs.putAll(fixingTs);
        _nameForCurveType = null;
        return this;
      }

      @Override
      public DiscountingMethodCurveBuilder2 getBuilder() {
        return new DiscountingMethodCurveBuilder2(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _interpolatorForCurve, _knownData,
            _fixingTs);
      }

      @Override
      public ConfigBuilder2 copy() {
        return new ConfigBuilder2(_curveNames, _discountingCurves, _iborCurves, _overnightCurves, _nodes, _fixingTs, _interpolatorForCurve, _knownData, _nameForCurveType, _singleUnit);
      }

    }
  }

  @SuppressWarnings("unchecked")
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(
      final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators, final String[][] curveNames,
      final MulticurveProviderDiscount knownData, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final ZonedDateTime valuationDate,
      final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] initialGuess = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = CurveTestUtils.convert(definitions[i][j][k], fixingTs, valuationDate);
          initialGuess[k] = definitions[i][j][k].accept(CurveTestUtils.RATES_INITIALIZATION);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, discountingCurves, iborCurves, overnightCurves, PAR_SPREAD_CALCULATOR,
        PAR_SPREAD_SENSITIVITY_CALCULATOR);
  }

  @SuppressWarnings("unchecked")
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(
      final MultiCurveBundle[] curveBundles, final MulticurveProviderDiscount knownData, final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs,
      final ZonedDateTime valuationDate, final LinkedHashMap<String, Currency> discountingCurves, final LinkedHashMap<String, IborIndex[]> iborCurves,
      final LinkedHashMap<String, IndexON[]> overnightCurves) {
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, discountingCurves, iborCurves, overnightCurves, PAR_SPREAD_CALCULATOR,
        PAR_SPREAD_SENSITIVITY_CALCULATOR);
  }

  public static void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderDiscount curves,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final FXMatrix fxMatrix, final ZonedDateTime valuationDate) {
    final int nbBlocks = definitions.length;
    for (int i = 0; i < nbBlocks; i++) {
      final InstrumentDerivative[][] instruments = CurveTestUtils.convert(definitions[i], fixingTs, valuationDate);
      for (final InstrumentDerivative[] instrumentsForCurve : instruments) {
        for (final InstrumentDerivative instrument : instrumentsForCurve) {
          final MultipleCurrencyAmount pv = instrument.accept(PVC, curves);
          final double usdPv = fxMatrix.convert(pv, Currency.USD).getAmount();
          assertEquals(usdPv, 0, 1e-9);
        }
      }
    }
  }

  public static void curveConstructionTest(final InstrumentDefinition<?>[] definitions, final MulticurveProviderDiscount curves,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final FXMatrix fxMatrix, final ZonedDateTime valuationDate,
      final Currency valuationCurrency) {
    for (final InstrumentDefinition<?> definition : definitions) {
      final InstrumentDerivative instrument = CurveTestUtils.convert(definition, fixingTs, valuationDate);
      final MultipleCurrencyAmount pv = instrument.accept(PVC, curves);
      final double valuationCcyPv = fxMatrix.convert(pv, valuationCurrency).getAmount();
      assertEquals(valuationCcyPv, 0, 1e-9);
    }
  }
}
