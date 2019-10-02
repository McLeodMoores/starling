/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.security;

import static com.opengamma.bbg.util.BloombergSecurityUtils.AAPL_EQUITY_TICKER;
import static com.opengamma.bbg.util.BloombergSecurityUtils.APV_EQUITY_OPTION_TICKER;
import static com.opengamma.bbg.util.BloombergSecurityUtils.ATT_EQUITY_TICKER;
import static com.opengamma.bbg.util.BloombergSecurityUtils.EUR;
import static com.opengamma.bbg.util.BloombergSecurityUtils.SPX_INDEX_OPTION_TICKER;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeAPVLEquityOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeAUDUSDCurrencyFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeAgricultureFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEquityFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEthanolFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeExchangeTradedFund;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeExpectedAAPLEquitySecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeExpectedATTEquitySecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeInterestRateFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeSPXIndexOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeSilverFuture;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.joda.beans.Bean;
import org.joda.beans.test.BeanAssert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;

/**
 * Base case for testing BloombergSecuritySource.
 */
/**
 * @author emcleod
 *
 */
/**
 * @author emcleod
 *
 */
@Test(groups = TestGroup.INTEGRATION)
public class BloombergSecurityProviderTest {

  private static final EquitySecurity EXPECTED_AAPL_EQUITY_SEC = makeExpectedAAPLEquitySecurity();
  private static final EquitySecurity EXPECTED_ATT_EQUITY_SEC = makeExpectedATTEquitySecurity();
  private static final EquityOptionSecurity EXPECTED_APVL_EQUITYOPTION_SEC = makeAPVLEquityOptionSecurity();
  private static final EquityIndexOptionSecurity EXPECTED_SPX_INDEXOPTION_SEC = makeSPXIndexOptionSecurity();
  private static final FXFutureSecurity EXPECTED_AUDUSD_FUTURE_SEC = makeAUDUSDCurrencyFuture();
  private static final MetalFutureSecurity EXPECTED_SILVER_FUTURE = makeSilverFuture();
  private static final EnergyFutureSecurity EXPECTED_ETHANOL_FUTURE = makeEthanolFuture();
  private static final InterestRateFutureSecurity EXPECTED_EURODOLLAR_FUTURE = makeInterestRateFuture();
  private static final AgricultureFutureSecurity EXPECTED_WHEAT_FUTURE_SEC = makeAgricultureFuture();
  private static final EquityFutureSecurity EXPECTED_EQUITY_FUTURE_SEC = makeEquityFuture();
  private static final EquitySecurity US_NATURAL_GAS_FUND = makeExchangeTradedFund();

  private static final ExternalScheme[] EXPECTED_IDENTIFICATION_SCHEME = new ExternalScheme[] {
      ExternalSchemes.BLOOMBERG_BUID, ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.CUSIP };

  private BloombergReferenceDataProvider _refDataProvider;
  private SecurityProvider _securityProvider;

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @BeforeClass
  public void setupSecurityProvider() throws Exception {
    _securityProvider = createSecurityProvider();
  }

  /**
   * @return the security provider
   * @throws Exception
   *           if there is an unexpected exception
   */
  protected SecurityProvider createSecurityProvider() throws Exception {
    final BloombergConnector connector = BloombergTestUtils.getBloombergConnector();
    final BloombergReferenceDataProvider refDataProvider = new BloombergReferenceDataProvider(connector);
    refDataProvider.start();
    _refDataProvider = refDataProvider;
    final ExchangeDataProvider exchangeProvider = DefaultExchangeDataProvider.getInstance();
    return new BloombergSecurityProvider(refDataProvider, exchangeProvider);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @AfterClass
  public void terminateSecurityProvider() throws Exception {
    stopSecurityProvider(_securityProvider);
    _securityProvider = null;
  }

  /**
   * @param provider
   *          the provider
   * @throws Exception
   *           if there is a problem
   */
  protected void stopSecurityProvider(final SecurityProvider provider) throws Exception {
    if (_refDataProvider != null) {
      final BloombergReferenceDataProvider dataProvider = _refDataProvider;
      _refDataProvider = null;
      dataProvider.stop();
    }
  }

  // -------------------------------------------------------------------------
  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void aaplEquityByBbgTicker() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_AAPL_EQUITY_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    assertNotNull(bloombergIdentifier);
    final Security sec = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(sec);
    assertEquitySecurity(EXPECTED_AAPL_EQUITY_SEC, sec);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void aaplEquityByBbgUnique() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_AAPL_EQUITY_SEC, ExternalSchemes.BLOOMBERG_BUID);
    assertNotNull(bloombergIdentifier);
    final Security sec = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertEquitySecurity(EXPECTED_AAPL_EQUITY_SEC, sec);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  public void attEquityByBbgTicker() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_ATT_EQUITY_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    final Security sec = _securityProvider.getSecurity(ExternalIdBundle.of(bloombergIdentifier));
    assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, sec);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void exchangeTradedFund() throws Exception {
    final Security security = _securityProvider.getSecurity(US_NATURAL_GAS_FUND.getExternalIdBundle());
    assertEquitySecurity(US_NATURAL_GAS_FUND, security);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void attEquitiesByBbgTicker() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_ATT_EQUITY_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    final Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, security);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void attEquityByBbgUnique() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_ATT_EQUITY_SEC, ExternalSchemes.BLOOMBERG_BUID);
    final Security sec = _securityProvider.getSecurity(ExternalIdBundle.of(bloombergIdentifier));
    assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, sec);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void attEquitiesByBbgUnique() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_ATT_EQUITY_SEC, ExternalSchemes.BLOOMBERG_BUID);
    final Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, security);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void apvEquityOptionByBbgTicker() throws Exception {
    final Security sec = _securityProvider.getSecurity(EXPECTED_APVL_EQUITYOPTION_SEC.getExternalIdBundle());
    assertAmericanVanillaEquityOptionSecurity(EXPECTED_APVL_EQUITYOPTION_SEC, sec);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void apvEquityOptionsByBbgTicker() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_APVL_EQUITYOPTION_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    final Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertAmericanVanillaEquityOptionSecurity(EXPECTED_APVL_EQUITYOPTION_SEC, security);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void apvEquityOptionsByBbgUnique() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_APVL_EQUITYOPTION_SEC, ExternalSchemes.BLOOMBERG_BUID);
    final Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertAmericanVanillaEquityOptionSecurity(EXPECTED_APVL_EQUITYOPTION_SEC, security);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void apvEquityOptionByBbgUnique() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_APVL_EQUITYOPTION_SEC, ExternalSchemes.BLOOMBERG_BUID);
    final Security sec = _securityProvider.getSecurity(ExternalIdBundle.of(bloombergIdentifier));
    assertAmericanVanillaEquityOptionSecurity(EXPECTED_APVL_EQUITYOPTION_SEC, sec);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void spxIndexOptionByBbgTicker() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_SPX_INDEXOPTION_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    final Security sec = _securityProvider.getSecurity(ExternalIdBundle.of(bloombergIdentifier));
    assertEuropeanVanillaEquityIndexOptionSecurity(EXPECTED_SPX_INDEXOPTION_SEC, sec);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void spxIndexOptionsByBbgTicker() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_SPX_INDEXOPTION_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    final Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertEuropeanVanillaEquityIndexOptionSecurity(EXPECTED_SPX_INDEXOPTION_SEC, security);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void spxIndexOptionByBbgUnique() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_SPX_INDEXOPTION_SEC, ExternalSchemes.BLOOMBERG_BUID);
    final Security sec = _securityProvider.getSecurity(ExternalIdBundle.of(bloombergIdentifier));
    assertEuropeanVanillaEquityIndexOptionSecurity(EXPECTED_SPX_INDEXOPTION_SEC, sec);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void spxIndexOptionsByBbgUnique() throws Exception {
    final ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_SPX_INDEXOPTION_SEC, ExternalSchemes.BLOOMBERG_BUID);
    final Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertEuropeanVanillaEquityIndexOptionSecurity(EXPECTED_SPX_INDEXOPTION_SEC, security);
  }

  private static ExternalId getBloombergIdentifier(final FinancialSecurity finSec, final ExternalScheme scheme) {
    final ExternalIdBundle identifierBundle = finSec.getExternalIdBundle();
    return identifierBundle.getExternalId(scheme);
  }

  // -------------------------------------------------------------------------
  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test(groups = { "bbgSecurityFutureTests" })
  public void agricultureFuture() throws Exception {
    final Security wheat = _securityProvider.getSecurity(EXPECTED_WHEAT_FUTURE_SEC.getExternalIdBundle());
    assertNotNull(wheat);
    assertTrue(wheat instanceof AgricultureFutureSecurity);
    assertSecurity(EXPECTED_WHEAT_FUTURE_SEC, wheat);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test(groups = { "bbgSecurityFutureTests" })
  public void equityFuture() throws Exception {
    final Security spIndex = _securityProvider.getSecurity(EXPECTED_EQUITY_FUTURE_SEC.getExternalIdBundle());
    assertNotNull(spIndex);
    assertTrue(spIndex instanceof EquityFutureSecurity);
    assertSecurity(EXPECTED_EQUITY_FUTURE_SEC, spIndex);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test(enabled = false)
  public void currencyFuture() throws Exception {
    final ExternalIdBundle id = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("LNM0 Curncy"));
    final Security audUsd = _securityProvider.getSecurity(id);
    assertNotNull(audUsd);
    assertTrue(audUsd instanceof FXFutureSecurity);
    assertSecurity(EXPECTED_AUDUSD_FUTURE_SEC, audUsd);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test(groups = { "bbgSecurityFutureTests" })
  public void euroBondFuture() throws Exception {
    final ExternalIdBundle euroBund = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("RXA Comdty"));
    final Security bond = _securityProvider.getSecurity(euroBund);
    assertNotNull(bond);
    assertTrue(bond instanceof BondFutureSecurity);
    final BondFutureSecurity euroBondFuture = (BondFutureSecurity) bond;
    assertEquals("FUTURE", euroBondFuture.getSecurityType());
    assertEquals("Bond", euroBondFuture.getContractCategory());
    assertEquals(EUR, euroBondFuture.getCurrency());
    final String displayName = euroBondFuture.getName();
    assertNotNull(displayName);
    assertTrue(displayName.contains("EURO-BUND FUTURE"));
    final Expiry expiry = euroBondFuture.getExpiry();
    assertNotNull(expiry);
    assertTrue(expiry.toInstant().isAfter(getTodayInstant()));
    assertEquals("XEUR", euroBondFuture.getTradingExchange());
    assertEquals("XEUR", euroBondFuture.getSettlementExchange());
    // assert identifiers are set
    final Collection<ExternalId> identifiers = euroBondFuture.getExternalIdBundle().getExternalIds();
    assertNotNull(identifiers);
    assertTrue(identifiers.size() >= EXPECTED_IDENTIFICATION_SCHEME.length);
    final ExternalIdBundle identifierBundle = ExternalIdBundle.of(identifiers);
    for (final ExternalScheme expectedIDScheme : EXPECTED_IDENTIFICATION_SCHEME) {
      assertNotNull(identifierBundle.getExternalId(expectedIDScheme));
    }
    // assert deliverables are not empty
    final Collection<BondFutureDeliverable> basket = euroBondFuture.getBasket();
    assertNotNull(basket);
    for (final BondFutureDeliverable bondFutureDeliverable : basket) {
      final ExternalIdBundle bundle = bondFutureDeliverable.getIdentifiers();
      assertNotNull(bundle);
      assertNotNull(bundle.getExternalId(ExternalSchemes.BLOOMBERG_BUID));
      assertTrue(bondFutureDeliverable.getConversionFactor() > 0);
    }
  }

  private static Instant getTodayInstant() {
    final Instant toDay = Clock.systemUTC().instant();
    return toDay;
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test(groups = { "bbgSecurityFutureTests" })
  public void metalFuture() throws Exception {
    final Security silverFuture = _securityProvider.getSecurity(EXPECTED_SILVER_FUTURE.getExternalIdBundle());
    assertNotNull(silverFuture);
    assertTrue(silverFuture instanceof MetalFutureSecurity);
    assertSecurity(EXPECTED_SILVER_FUTURE, silverFuture);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test(groups = { "bbgSecurityFutureTests" })
  public void energyFuture() throws Exception {
    final Security ethanolFuture = _securityProvider.getSecurity(EXPECTED_ETHANOL_FUTURE.getExternalIdBundle());
    assertNotNull(ethanolFuture);
    assertTrue(ethanolFuture instanceof EnergyFutureSecurity);
    assertSecurity(EXPECTED_ETHANOL_FUTURE, ethanolFuture);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test(groups = { "bbgSecurityFutureTests" })
  public void interestRateFuture() throws Exception {
    final Security euroDollar = _securityProvider.getSecurity(EXPECTED_EURODOLLAR_FUTURE.getExternalIdBundle());
    assertNotNull(euroDollar);
    assertTrue(euroDollar instanceof InterestRateFutureSecurity);
    assertSecurity(EXPECTED_EURODOLLAR_FUTURE, euroDollar);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void invalidSecurity() throws Exception {
    final ExternalIdBundle invalidKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("INVALID"));
    final Security sec = _securityProvider.getSecurity(invalidKey);
    assertNull(sec);
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void invalidSecurities() throws Exception {
    final ExternalIdBundle invalidKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("INVALID"));
    final Map<ExternalIdBundle, Security> securities = _securityProvider.getSecurities(ImmutableSet.of(invalidKey));
    assertNotNull(securities);
    assertTrue(securities.isEmpty());
  }

  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void multiThreadedSecurityRequest() throws Exception {

    final ExternalIdBundle apvKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(APV_EQUITY_OPTION_TICKER));
    final ExternalIdBundle spxKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(SPX_INDEX_OPTION_TICKER));
    final ExternalIdBundle aaplKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(AAPL_EQUITY_TICKER));
    final ExternalIdBundle attKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(ATT_EQUITY_TICKER));

    final ExecutorService pool = Executors.newFixedThreadPool(4);
    final List<Future<Security>> apvresults = new ArrayList<>();
    final List<Future<Security>> spxresults = new ArrayList<>();
    final List<Future<Security>> aaplresults = new ArrayList<>();
    final List<Future<Security>> attresults = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      apvresults.add(pool.submit(new BSMGetSecurityCallable(apvKey)));
      spxresults.add(pool.submit(new BSMGetSecurityCallable(spxKey)));
      aaplresults.add(pool.submit(new BSMGetSecurityCallable(aaplKey)));
      attresults.add(pool.submit(new BSMGetSecurityCallable(attKey)));
    }

    for (final Future<Security> future : apvresults) {
      // Check that each one didn't throw an exception and returns the expected
      // APV security
      final Security sec = future.get();
      assertAmericanVanillaEquityOptionSecurity(EXPECTED_APVL_EQUITYOPTION_SEC, sec);
    }

    for (final Future<Security> future : spxresults) {
      // Check that each one didn't throw an exception and returns the expected
      // SPX security
      final Security sec = future.get();
      assertEuropeanVanillaEquityIndexOptionSecurity(EXPECTED_SPX_INDEXOPTION_SEC, sec);
    }

    for (final Future<Security> future : aaplresults) {
      // Check that each one didn't throw an exception and returns the expected
      // AAPL security
      final Security sec = future.get();
      assertEquitySecurity(EXPECTED_AAPL_EQUITY_SEC, sec);
    }

    for (final Future<Security> future : attresults) {
      // Check that each one didn't throw an exception and returns the expected
      // AT&T security
      final Security sec = future.get();
      assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, sec);
    }
  }

  /**
   * @param expected
   *          the expected security
   * @param actual
   *          the actual security
   */
  static void assertSecurity(final Security expected, final Security actual) {
    assertNotNull(actual);
    BeanAssert.assertBeanEquals((Bean) expected, (Bean) actual);
  }

  /**
   * @param expectedEquity
   *          the expected security
   * @param sec
   *          the actual security
   */
  static void assertEquitySecurity(final EquitySecurity expectedEquity, final Security sec) {
    final EquitySecurity clonedEquity = expectedEquity.clone();
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquitySecurity);
    final EquitySecurity actualEquity = (EquitySecurity) sec;
    assertEquals(clonedEquity.getSecurityType(), actualEquity.getSecurityType());

    final ExternalId expectedBUID = clonedEquity.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_BUID);
    final ExternalId actualBUID = actualEquity.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_BUID);
    assertEquals(expectedBUID, actualBUID);

    final ExternalId expectedTicker = clonedEquity.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    final ExternalId actualTicker = actualEquity.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    assertEquals(expectedTicker, actualTicker);

    assertEquals(clonedEquity.getUniqueId(), actualEquity.getUniqueId());
    assertEquals(clonedEquity.getShortName(), actualEquity.getShortName());
    assertEquals(clonedEquity.getExchange(), actualEquity.getExchange());
    assertEquals(clonedEquity.getCompanyName(), actualEquity.getCompanyName());
    assertEquals(clonedEquity.getCurrency(), actualEquity.getCurrency());

    // check the lot without Identifiers
    final ExternalIdBundle expectedIdentifiers = clonedEquity.getExternalIdBundle();
    final ExternalIdBundle actualIdentifiers = actualEquity.getExternalIdBundle();

    clonedEquity.setExternalIdBundle(ExternalIdBundle.EMPTY);
    actualEquity.setExternalIdBundle(ExternalIdBundle.EMPTY);
    assertEquals(clonedEquity, actualEquity);

    clonedEquity.setExternalIdBundle(expectedIdentifiers);
    actualEquity.setExternalIdBundle(actualIdentifiers);
  }

  /**
   * @param expectedOption
   *          the expected security
   * @param sec
   *          the actual security
   */
  static void assertAmericanVanillaEquityOptionSecurity(final EquityOptionSecurity expectedOption, final Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquityOptionSecurity);
    final EquityOptionSecurity equitySec = (EquityOptionSecurity) sec;
    assertTrue(equitySec.getExerciseType() instanceof AmericanExerciseType);
    assertEquityOptionSecurity(expectedOption, sec);
  }

  /**
   * @param expectedOption
   *          the expected security
   * @param sec
   *          the actual security
   */
  static void assertAmericanVanillaEquityIndexOptionSecurity(final EquityIndexOptionSecurity expectedOption, final Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquityIndexOptionSecurity);
    final EquityIndexOptionSecurity equityIndexOption = (EquityIndexOptionSecurity) sec;
    assertTrue(equityIndexOption.getExerciseType() instanceof AmericanExerciseType);
    assertEquityIndexOptionSecurity(expectedOption, sec);
  }

  /**
   * @param expectedOption
   *          the expected security
   * @param sec
   *          the actual security
   */
  static void assertEquityOptionSecurity(final EquityOptionSecurity expectedOption, final Security sec) {
    assertNotNull(expectedOption);
    assertNotNull(sec);
    final EquityOptionSecurity actualOption = (EquityOptionSecurity) sec;
    assertEquals(expectedOption.getExternalIdBundle(), actualOption.getExternalIdBundle());
    assertEquals(expectedOption.getUniqueId(), actualOption.getUniqueId());
    assertEquals(expectedOption.getSecurityType(), actualOption.getSecurityType());
    assertEquals(expectedOption.getCurrency(), actualOption.getCurrency());
    assertEquals(expectedOption.getOptionType(), actualOption.getOptionType());
    assertTrue(expectedOption.getStrike() == actualOption.getStrike());
    assertEquals(expectedOption.getExpiry(), actualOption.getExpiry());
    assertEquals(expectedOption.getUnderlyingId(), actualOption.getUnderlyingId());
    assertEquals(expectedOption.getName(), actualOption.getName());
    // check the lot
    assertSecurity(expectedOption, sec);
  }

  /**
   * @param expectedOption
   *          the expected security
   * @param sec
   *          the actual security
   */
  static void assertEquityIndexOptionSecurity(final EquityIndexOptionSecurity expectedOption, final Security sec) {
    assertNotNull(expectedOption);
    assertNotNull(sec);
    final EquityIndexOptionSecurity actualOption = (EquityIndexOptionSecurity) sec;
    assertEquals(expectedOption.getExternalIdBundle(), actualOption.getExternalIdBundle());
    assertEquals(expectedOption.getUniqueId(), actualOption.getUniqueId());
    assertEquals(expectedOption.getSecurityType(), actualOption.getSecurityType());
    assertEquals(expectedOption.getCurrency(), actualOption.getCurrency());
    assertEquals(expectedOption.getOptionType(), actualOption.getOptionType());
    assertTrue(expectedOption.getStrike() == actualOption.getStrike());
    assertEquals(expectedOption.getExpiry(), actualOption.getExpiry());
    assertEquals(expectedOption.getUnderlyingId(), actualOption.getUnderlyingId());
    assertEquals(expectedOption.getName(), actualOption.getName());
    // check the lot
    assertSecurity(expectedOption, sec);
  }

  /**
   * @param expectedOption
   *          the expected security
   * @param sec
   *          the actual security
   */
  static void assertEuropeanVanillaEquityOptionSecurity(final EquityOptionSecurity expectedOption, final Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquityOptionSecurity);
    final EquityOptionSecurity equitySec = (EquityOptionSecurity) sec;
    assertTrue(equitySec.getExerciseType() instanceof EuropeanExerciseType);
    assertEquityOptionSecurity(expectedOption, sec);
  }

  /**
   * @param expectedOption
   *          the expected security
   * @param sec
   *          the actual security
   */
  static void assertEuropeanVanillaEquityIndexOptionSecurity(final EquityIndexOptionSecurity expectedOption, final Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquityIndexOptionSecurity);
    final EquityIndexOptionSecurity equityIndexOption = (EquityIndexOptionSecurity) sec;
    assertTrue(equityIndexOption.getExerciseType() instanceof EuropeanExerciseType);
    assertEquityIndexOptionSecurity(expectedOption, sec);
  }

  /**
   *
   */
  private class BSMGetSecurityCallable implements Callable<Security> {
    private final ExternalIdBundle _secKey;

    BSMGetSecurityCallable(final ExternalIdBundle secKey) {
      _secKey = secKey;
    }

    @Override
    public Security call() throws Exception {
      return _securityProvider.getSecurity(_secKey);
    }
  }

  // -------------------------------------------------------------------------
  /**
   * @throws Exception
   *           if there is an unexpected exception
   */
  @Test
  public void testGetBulkSecurity() throws Exception {
    final ExternalIdBundle aaplId = getBloombergIdentifier(EXPECTED_AAPL_EQUITY_SEC, ExternalSchemes.BLOOMBERG_BUID).toBundle();
    final ExternalIdBundle attId = getBloombergIdentifier(EXPECTED_ATT_EQUITY_SEC, ExternalSchemes.BLOOMBERG_BUID).toBundle();

    final Map<ExternalIdBundle, Security> securities = _securityProvider.getSecurities(ImmutableSet.of(aaplId, attId));
    assertNotNull(securities);
    assertEquals(2, securities.size());
    assertTrue(securities.keySet().contains(aaplId));
    assertTrue(securities.keySet().contains(attId));
    assertEquitySecurity(EXPECTED_AAPL_EQUITY_SEC, securities.get(aaplId));
    assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, securities.get(attId));

  }

}
