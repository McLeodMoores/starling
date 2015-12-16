/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;

import com.google.common.collect.Sets;
import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.future.QuandlFedFundsFutureCurveInstrumentProvider;
import com.mcleodmoores.quandl.future.QuandlFutureCurveInstrumentProvider;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Generates {@link CurveConstructionConfiguration}, {@link AbstractCurveDefinition} and {@link CurveNodeIdMapper} configurations.
 * The configurations use Quandl tickers.
 */
public final class QuandlCurveConfigurationsGenerator {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlCurveConfigurationsGenerator.class);
  /** The ibor convention names for each currency */
  private static final Map<Currency, String> IBOR_CONVENTIONS = new HashMap<>();
  /** The ibor future prefixes for each currency / underlying tenor pair */
  private static final Map<Pair<Currency, Tenor>, String> STIR_FUTURE_PREFIXES = new HashMap<>();
  /** The STIR future conventions for each currency / underlying tenor pair */
  private static final Map<Pair<Currency, Tenor>, String> STIR_FUTURE_CONVENTIONS = new HashMap<>();
  /** The number of (quarterly) futures to use */
  private static final Map<Currency, Integer> N_STIR_FUTURES = new HashMap<>();

  static {
    IBOR_CONVENTIONS.put(Currency.CHF, "CHF ICE LIBOR");
    IBOR_CONVENTIONS.put(Currency.EUR, "EUR EBF EURIBOR");
    IBOR_CONVENTIONS.put(Currency.GBP, "GBP ICE LIBOR");
    IBOR_CONVENTIONS.put(Currency.JPY, "JPY JBA Euroyen TIBOR");
    STIR_FUTURE_PREFIXES.put(Pairs.of(Currency.CHF, Tenor.THREE_MONTHS), "LIFFE/S");
    STIR_FUTURE_PREFIXES.put(Pairs.of(Currency.EUR, Tenor.THREE_MONTHS), "LIFFE/I");
    STIR_FUTURE_PREFIXES.put(Pairs.of(Currency.GBP, Tenor.THREE_MONTHS), "LIFFE/L");
    STIR_FUTURE_PREFIXES.put(Pairs.of(Currency.JPY, Tenor.THREE_MONTHS), "TFX/JBA");
    STIR_FUTURE_CONVENTIONS.put(Pairs.of(Currency.CHF, Tenor.THREE_MONTHS), "LIFFE 3M/3M Euroswiss LIBOR");
    STIR_FUTURE_CONVENTIONS.put(Pairs.of(Currency.EUR, Tenor.THREE_MONTHS), "LIFFE 3M/3M EURIBOR");
    STIR_FUTURE_CONVENTIONS.put(Pairs.of(Currency.GBP, Tenor.THREE_MONTHS), "LIFFE 3M/3M Short Sterling LIBOR");
    STIR_FUTURE_CONVENTIONS.put(Pairs.of(Currency.JPY, Tenor.THREE_MONTHS), "LIFFE 3M/3M Euroyen TIBOR");
    N_STIR_FUTURES.put(Currency.CHF, 15);
    N_STIR_FUTURES.put(Currency.EUR, 16);
    N_STIR_FUTURES.put(Currency.GBP, 16);
    N_STIR_FUTURES.put(Currency.JPY, 10);
  }

  /**
   * Restricted constructor.
   */
  private QuandlCurveConfigurationsGenerator() {
  }

  /**
   * Generates the three types of configurations needed to construct curves: the interpolated curve definition, the
   * curve node id mapper and the curve construction configuration.
   * @return The configurations.
   */
  public static Configurations createConfigurations() {
    final Collection<CurveConstructionConfiguration> cccs = new HashSet<>();
    final Collection<AbstractCurveDefinition> acds = new HashSet<>();
    final Collection<CurveNodeIdMapper> cnims = new HashSet<>();
    final Configurations configurations = new Configurations(cccs, acds, cnims);
    configurations.addAll(createUsdConfigurations());
    configurations.addAll(createStirFutureOnlyConfigurations(Currency.CHF, Tenor.THREE_MONTHS, "LIBOR"));
    configurations.addAll(createStirFutureOnlyConfigurations(Currency.EUR, Tenor.THREE_MONTHS, "EURIBOR"));
    configurations.addAll(createStirFutureOnlyConfigurations(Currency.GBP, Tenor.THREE_MONTHS, "LIBOR"));
    configurations.addAll(createStirFutureOnlyConfigurations(Currency.JPY, Tenor.THREE_MONTHS, "TIBOR"));
    return configurations;
  }

  /**
   * Creates a two-curve configuration for USD - a discounting / overnight curve that uses Fed fund futures
   * and a 3m Libor curve that uses STIR futures and vanilla fixed / Libor swaps.
   * @return  the configurations
   */
  private static Configurations createUsdConfigurations() {
    final String curveConstructionConfigurationName = "USD Rates";
    final String overnightCurveName = "USD Effective Federal Funds";
    final String overnightConventionName = "USD Effective Federal Funds";
    final String overnightCurveNodeIdMapperName = "Quandl Overnight USD ";
    final ExternalId overnightReferenceId = QuandlConstants.ofCode("FRED/USDONTD156N");
    final String liborCurveName = "USD 3m LIBOR";
    final String liborConventionName = "USD ICE LIBOR";
    final String liborCurveNodeIdMapperName = "Quandl 3m LIBOR USD";
    final ExternalId liborReferenceId = QuandlConstants.ofCode("FRED/USD3MTD156N");
    // use the same curve for discounting and overnight
    final List<CurveTypeConfiguration> discountingAndOvernight = new ArrayList<>();
    discountingAndOvernight.add(new DiscountingCurveTypeConfiguration("USD"));
    discountingAndOvernight.add(new OvernightCurveTypeConfiguration(overnightReferenceId));
    // three month ibor
    final List<CurveTypeConfiguration> ibor = new ArrayList<>();
    ibor.add(new IborCurveTypeConfiguration(liborReferenceId, Tenor.THREE_MONTHS));
    // create two curve groups, as the curves are not coupled
    final List<CurveGroupConfiguration> cgc = new ArrayList<>();
    cgc.add(new CurveGroupConfiguration(0,
        Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap(overnightCurveName, discountingAndOvernight)));
    cgc.add(new CurveGroupConfiguration(1, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap(liborCurveName, ibor)));
    // create the curve construction configuration
    final CurveConstructionConfiguration config = new CurveConstructionConfiguration(curveConstructionConfigurationName, cgc, Collections.<String>emptyList());
    // populate the discounting / overnight curve with cash and rate futures
    final Set<CurveNode> overnightCurveNodes = new HashSet<>();
    final Map<Tenor, CurveInstrumentProvider> overnightId = new LinkedHashMap<>();
    final Map<Tenor, CurveInstrumentProvider> fedFundFutureIds = new LinkedHashMap<>();
    populateOvernight(overnightCurveNodes, overnightId, overnightCurveNodeIdMapperName, overnightConventionName, overnightReferenceId);
    populateFedFundsFuture(overnightCurveNodes, fedFundFutureIds, overnightCurveNodeIdMapperName, Tenor.ONE_MONTH, Tenor.ONE_MONTH, "CME 30D Fed Funds", "CME/FF",
        overnightReferenceId, 16);
    final AbstractCurveDefinition overnightDefinition = new InterpolatedCurveDefinition(overnightCurveName, overnightCurveNodes,
        Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final CurveNodeIdMapper overnightMapper = CurveNodeIdMapper.builder()
        .name(overnightCurveNodeIdMapperName)
        .cashNodeIds(overnightId)
        .rateFutureNodeIds(fedFundFutureIds)
        .build();
    // populate the 3m libor curve with cash, rate futures and swaps
    final Set<CurveNode> liborCurveNodes = new HashSet<>();
    final Map<Tenor, CurveInstrumentProvider> liborId = new LinkedHashMap<>();
    final Map<Tenor, CurveInstrumentProvider> stirFutureIds = new LinkedHashMap<>();
    final Map<Tenor, CurveInstrumentProvider> swapIds = new LinkedHashMap<>();
    populateIbor(liborCurveNodes, liborId, liborCurveNodeIdMapperName, Tenor.THREE_MONTHS, liborConventionName, liborReferenceId);
    populateRateFuture(liborCurveNodes, stirFutureIds, liborCurveNodeIdMapperName, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS,
        "CME 3M/3M Eurodollar LIBOR", "CME/ED", 16);
    populateSwaps(liborCurveNodes, swapIds, liborCurveNodeIdMapperName, "USD Vanilla LIBOR Fixed", "USD Vanilla LIBOR", "FRED/DSWP");
    final AbstractCurveDefinition liborDefinition = new InterpolatedCurveDefinition(liborCurveName, liborCurveNodes,
        Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final CurveNodeIdMapper liborMapper = CurveNodeIdMapper.builder()
        .name(liborCurveNodeIdMapperName)
        .cashNodeIds(liborId)
        .rateFutureNodeIds(stirFutureIds)
        .swapNodeIds(swapIds)
        .build();
    return new Configurations(Collections.singleton(config), Sets.newHashSet(overnightDefinition, liborDefinition),
        Sets.newHashSet(overnightMapper, liborMapper));
  }

  /**
   * Creates a single-curve configuration for a currency - a discounting / 3m Ibor curve that uses only STIR
   * futures.
   * @param currency  the currency
   * @param iborTenor  the ibor tenor
   * @param rateName  the ibor rate name (e.g. LIBOR, TIBOR)
   * @return  the configurations
   */
  private static Configurations createStirFutureOnlyConfigurations(final Currency currency, final Tenor iborTenor, final String rateName) {
    final String tenorString = iborTenor.toFormattedString().substring(1).toLowerCase();
    final String curveConstructionConfigurationName = currency.getCode() + " Rates (" + tenorString + " basis)";
    final String curveName = currency.getCode() + " " + tenorString + " " + rateName;
    final String curveNodeIdMapperName = "Quandl " + tenorString + " " + currency.getCode() + " " + rateName;
    final Pair<Currency, Tenor> currencyTenor = Pairs.of(currency, iborTenor);
    final String iborConventionName = IBOR_CONVENTIONS.get(currency);
    if (iborConventionName == null) {
      LOGGER.error("Could not get ibor convention for {}, not generating configurations", currency);
      return null;
    }
    final String futureConvention = STIR_FUTURE_CONVENTIONS.get(currencyTenor);
    if (futureConvention == null) {
      LOGGER.error("Could not get STIR future convention for ({}, {}), not generating configurations", currency, iborTenor);
      return null;
    }
    final String futurePrefix = STIR_FUTURE_PREFIXES.get(currencyTenor);
    if (futurePrefix == null) {
      LOGGER.error("Could not get STIR future prefix for ({}, {}), not generating configurations", currency, iborTenor);
      return null;
    }
    // note this is using the convention name because there is no ibor rate available for this currency
    final ExternalId iborReferenceId = ExternalId.of("CONVENTION", iborConventionName);
    // use the same curve for discounting and ibor
    final List<CurveTypeConfiguration> ctc = new ArrayList<>();
    ctc.add(new DiscountingCurveTypeConfiguration(currency.getCode()));
    ctc.add(new IborCurveTypeConfiguration(iborReferenceId, iborTenor));
    // create a single curve group
    final List<CurveGroupConfiguration> cgc = new ArrayList<>();
    cgc.add(new CurveGroupConfiguration(0, Collections.<String, List<? extends CurveTypeConfiguration>>singletonMap(curveName, ctc)));
    // create the curve construction configuration
    final CurveConstructionConfiguration config = new CurveConstructionConfiguration(curveConstructionConfigurationName, cgc, Collections.<String>emptyList());
    // populate the curve definition with rate future nodes
    final Set<CurveNode> curveNodes = new HashSet<>();
    final Map<Tenor, CurveInstrumentProvider> rateFutureIds = new LinkedHashMap<>();
    final Integer nFutures = N_STIR_FUTURES.get(currency);
    if (nFutures == null) {
      LOGGER.error("Could not get number of futures to be added for {}, not generating configurations", currency);
      return null;
    }
    populateRateFuture(curveNodes, rateFutureIds, curveNodeIdMapperName, Tenor.THREE_MONTHS, iborTenor, futureConvention, futurePrefix, nFutures);
    final AbstractCurveDefinition definition = new InterpolatedCurveDefinition(curveName, curveNodes, Interpolator1DFactory.DOUBLE_QUADRATIC,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final CurveNodeIdMapper mapper = CurveNodeIdMapper.builder()
        .name(curveNodeIdMapperName)
        .rateFutureNodeIds(rateFutureIds)
        .build();
    return new Configurations(Collections.singleton(config), Collections.singleton(definition), Collections.singleton(mapper));
  }

  /**
   * Adds an overnight cash node to the configurations.
   * @param curveNodes  the curve nodes
   * @param overnightId  the overnight id
   * @param curveNodeIdMapperName  the curve node id mapper name
   * @param convention  the overnight convention name
   * @param overnightCode  the Quandl overnight code
   */
  private static void populateOvernight(final Set<CurveNode> curveNodes, final Map<Tenor, CurveInstrumentProvider> overnightId,
      final String curveNodeIdMapperName, final String convention, final ExternalId overnightCode) {
    final ExternalId conventionId = ExternalId.of("CONVENTION", convention);
    curveNodes.add(new CashNode(Tenor.of(Period.ZERO), Tenor.ON, conventionId, curveNodeIdMapperName));
    overnightId.put(Tenor.ON, new StaticCurveInstrumentProvider(overnightCode, MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
  }

  /**
   * Adds an ibor cash node to the configurations.
   * @param curveNodes  the curve nodes
   * @param iborId  the ibor id
   * @param curveNodeIdMapperName  the curve node id mapper name
   * @param tenor  the ibor tenor
   * @param convention  the ibor convention name
   * @param iborCode  the Quandl ibor code
   */
  private static void populateIbor(final Set<CurveNode> curveNodes, final Map<Tenor, CurveInstrumentProvider> iborId, final String curveNodeIdMapperName,
      final Tenor tenor, final String convention, final ExternalId iborCode) {
    final ExternalId conventionId = ExternalId.of("CONVENTION", convention);
    curveNodes.add(new CashNode(Tenor.of(Period.ZERO), tenor, conventionId, curveNodeIdMapperName));
    iborId.put(tenor, new StaticCurveInstrumentProvider(iborCode, MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
  }

  /**
   * Adds the 2nd to nth Fed fund future nodes to the configurations.
   * @param curveNodes  the curve nodes
   * @param rateFutureIds  the rate future ids
   * @param curveNodeIdMapperName  the curve node id mapper name
   * @param futureTenor  the future tenor
   * @param underlyingTenor  the underlying index tenor
   * @param convention  the future convention name
   * @param prefix  the Quandl future prefix
   * @param underlyingId  the Quandl code for the overnight rate
   * @param nFutures  the number of futures to add
   */
  private static void populateFedFundsFuture(final Set<CurveNode> curveNodes, final Map<Tenor, CurveInstrumentProvider> rateFutureIds,
      final String curveNodeIdMapperName, final Tenor futureTenor, final Tenor underlyingTenor, final String convention,
      final String prefix, final ExternalId underlyingId, final int nFutures) {
    final ExternalId conventionId = ExternalId.of("CONVENTION", convention);
    for (int i = 2; i < nFutures; i++) {
      curveNodes.add(new RateFutureNode(i, Tenor.of(Period.ZERO), futureTenor, underlyingTenor, conventionId, curveNodeIdMapperName));
    }
    rateFutureIds.put(Tenor.of(Period.ZERO),
        new QuandlFedFundsFutureCurveInstrumentProvider(prefix, MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT,
            underlyingId, MarketDataRequirementNames.MARKET_VALUE));
  }

  /**
   * Adds the 2nd to nth future nodes to the configurations.
   * @param curveNodes  the curve nodes
   * @param rateFutureIds  the rate future ids
   * @param curveNodeIdMapperName  the curve node id mapper name
   * @param futureTenor  the future tenor
   * @param underlyingTenor  the underlying index tenor
   * @param convention  the future convention name
   * @param prefix  the Quandl future prefix
   * @param nFutures  the number of futures to add
   */
  private static void populateRateFuture(final Set<CurveNode> curveNodes, final Map<Tenor, CurveInstrumentProvider> rateFutureIds,
      final String curveNodeIdMapperName, final Tenor futureTenor, final Tenor underlyingTenor, final String convention,
      final String prefix, final int nFutures) {
    final ExternalId conventionId = ExternalId.of("CONVENTION", convention);
    for (int i = 2; i < nFutures; i++) {
      curveNodes.add(new RateFutureNode(i, Tenor.of(Period.ZERO), futureTenor, underlyingTenor, conventionId, curveNodeIdMapperName));
    }
    rateFutureIds.put(Tenor.of(Period.ZERO),
        new QuandlFutureCurveInstrumentProvider(prefix, MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
  }

  /**
   * Adds 2y, 3y, 5y, 7y, 10y and 30y vanilla fixed / ibor swap nodes to the configurations.
   * @param curveNodes  the curve nodes
   * @param swapIds  the swap ids
   * @param curveNodeIdMapperName  the curve node id mapper name
   * @param payLegConvention  the pay leg convention name
   * @param receiveLegConvention  the receive leg convention name
   * @param prefix  the Quandl swap code prefix
   */
  private static void populateSwaps(final Set<CurveNode> curveNodes, final Map<Tenor, CurveInstrumentProvider> swapIds, final String curveNodeIdMapperName,
      final String payLegConvention, final String receiveLegConvention, final String prefix) {
    final ExternalId payLegConventionId = ExternalId.of("CONVENTION", payLegConvention);
    final ExternalId receiveLegConventionId = ExternalId.of("CONVENTION", receiveLegConvention);
    for (final Tenor tenor : new Tenor[] {Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS, Tenor.SEVEN_YEARS, Tenor.TEN_YEARS, Tenor.ofYears(30)}) {
      final CurveNode node = new SwapNode(Tenor.of(Period.ZERO), tenor, payLegConventionId, receiveLegConventionId, curveNodeIdMapperName);
      curveNodes.add(node);
      final String code = prefix + tenor.getPeriod().getYears();
      swapIds.put(tenor, new StaticCurveInstrumentProvider(QuandlConstants.ofCode(code), MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
    }
  }

  /**
   * Containers for configurations.
   */
  static class Configurations {
    /** The curve construction configurations */
    private final Collection<CurveConstructionConfiguration> _cccs;
    /** The curve definitions */
    private final Collection<AbstractCurveDefinition> _acds;
    /** The curve node id mappers */
    private final Collection<CurveNodeIdMapper> _cnims;

    /**
     * Creates an instance.
     * @param cccs  a collection of {@link CurveConstructionConfiguration}, not null
     * @param acds  a collection of {@link AbstractCurveDefinition}, not null
     * @param cnims  a collection of {@link CurveNodeIdMapper}, not null
     */
    Configurations(final Collection<CurveConstructionConfiguration> cccs, final Collection<AbstractCurveDefinition> acds,
        final Collection<CurveNodeIdMapper> cnims) {
      ArgumentChecker.notNull(cccs, "cccs");
      ArgumentChecker.notNull(acds, "acds");
      ArgumentChecker.notNull(cnims, "cnims");
      _cccs = new HashSet<>(cccs);
      _acds = new HashSet<>(acds);
      _cnims = new HashSet<>(cnims);
    }

    /**
     * Gets the {@link CurveConstructionConfiguration}s.
     * @return  the collection of configurations
     */
    public Collection<CurveConstructionConfiguration> getCurveConstructionConfigurations() {
      return _cccs;
    }

    /**
     * Gets the {@link AbstractCurveDefinition}s.
     * @return  the collection of configurations
     */
    public Collection<AbstractCurveDefinition> getAbstractCurveDefinitions() {
      return _acds;
    }

    /**
     * Gets the {@link CurveNodeIdMapper}s.
     * @return  the collection of configurations
     */
    public Collection<CurveNodeIdMapper> getCurveNodeIdMappers() {
      return _cnims;
    }

    /**
     * Adds all configurations. If the configurations are null, does nothing.
     * @param configurations  the configurations
     */
    public void addAll(final Configurations configurations) {
      if (configurations == null) {
        return;
      }
      _cccs.addAll(configurations.getCurveConstructionConfigurations());
      _acds.addAll(configurations.getAbstractCurveDefinitions());
      _cnims.addAll(configurations.getCurveNodeIdMappers());
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _acds.hashCode();
      result = prime * result + _cccs.hashCode();
      result = prime * result + _cnims.hashCode();
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final Configurations other = (Configurations) obj;
      if (!Objects.equals(_cccs, other._cccs)) {
        return false;
      }
      if (!Objects.equals(_acds, other._acds)) {
        return false;
      }
      if (!Objects.equals(_cnims, other._cnims)) {
        return false;
      }
      return true;
    }


  }
}
