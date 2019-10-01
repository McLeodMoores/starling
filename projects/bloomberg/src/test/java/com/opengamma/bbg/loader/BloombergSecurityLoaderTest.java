/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.util.BloombergSecurityUtils.makeAPVLEquityOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeCommodityFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEURIBORFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEURODOLLARFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEquityIndexDividendFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEquityIndexFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeExpectedAAPLEquitySecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeFxFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeInterestRateFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeLIBORFutureOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeSPXIndexOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeUSBondFuture;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.security.BloombergSecurityProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.DefaultSecurityLoader;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDetailProvider;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterFiles;
import com.opengamma.util.db.DbConnectorFactoryBean;
import com.opengamma.util.db.HibernateMappingFiles;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION, singleThreaded = true)
public class BloombergSecurityLoaderTest extends AbstractDbTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(BloombergSecurityLoaderTest.class);

  private BloombergReferenceDataProvider _bbgProvider;
  private DbSecurityMaster _securityMaster;
  private DefaultSecurityLoader _securityLoader;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public BloombergSecurityLoaderTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
    LOGGER.info("running testcases for {}", databaseType);
  }

  // -------------------------------------------------------------------------
  @Override
  protected Class<?> dbConnectorScope() {
    return BloombergSecurityLoaderTest.class;
  }

  @Override
  protected void initDbConnectorFactory(final DbConnectorFactoryBean factory) {
    factory.setHibernateMappingFiles(new HibernateMappingFiles[] { new HibernateSecurityMasterFiles() });
  }

  // -------------------------------------------------------------------------
  @Override
  protected void doSetUpClass() {
    _bbgProvider = BloombergTestUtils.getBloombergReferenceDataProvider();
    _bbgProvider.start();
    final ReferenceDataProvider cachingProvider = BloombergTestUtils.getMongoCachingReferenceDataProvider(_bbgProvider);
    final ExchangeDataProvider exchangeProvider = DefaultExchangeDataProvider.getInstance();
    final BloombergSecurityProvider secProvider = new BloombergSecurityProvider(cachingProvider, exchangeProvider);
    _securityMaster = new DbSecurityMaster(getDbConnector());
    _securityMaster.setDetailProvider(new HibernateSecurityMasterDetailProvider());
    _securityLoader = new DefaultSecurityLoader(_securityMaster, secProvider);
  }

  @Override
  protected void doTearDownClass() {
    if (_bbgProvider != null) {
      _bbgProvider.stop();
      _bbgProvider = null;
    }
    _securityMaster = null;
  }

  // -------------------------------------------------------------------------
  private void assertLoadAndSaveSecurity(final FinancialSecurity expected) {
    // test we can load security from bloomberg
    final ExternalIdBundle identifierBundle = expected.getExternalIdBundle();

    final Map<ExternalIdBundle, UniqueId> loadedSecurities = _securityLoader.loadSecurities(Collections.singleton(identifierBundle));
    assertNotNull(loadedSecurities);
    assertEquals(1, loadedSecurities.size());
    final UniqueId uid = loadedSecurities.get(identifierBundle);
    assertNotNull(uid);

    // test we can add and read from secmaster
    final SecurityDocument securityDocument = _securityMaster.get(uid);
    assertNotNull(securityDocument);

    final Security fromSecMaster = securityDocument.getSecurity();
    assertNotNull(fromSecMaster);

    expected.accept(new FinancialSecurityVisitorAdapter<Void>() {

      private void assertSecurity() {
        fail();
      }

      @Override
      public Void visitCorporateBondSecurity(final CorporateBondSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitCashSecurity(final CashSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitCashFlowSecurity(final CashFlowSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitEquitySecurity(final EquitySecurity security) {
        assertTrue(fromSecMaster instanceof EquitySecurity);
        final EquitySecurity actual = (EquitySecurity) fromSecMaster;

        assertEquals(security.getCompanyName(), actual.getCompanyName());
        assertEquals(security.getCurrency(), actual.getCurrency());
        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExchangeCode(), actual.getExchangeCode());
        assertEquals(security.getGicsCode(), actual.getGicsCode());
        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertEquals(security.getShortName(), actual.getShortName());
        assertNotNull(actual.getUniqueId());
        return null;
      }

      @Override
      public Void visitFRASecurity(final FRASecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitBondFutureSecurity(final BondFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitEquityFutureSecurity(final EquityFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitFXFutureSecurity(final FXFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitIndexFutureSecurity(final IndexFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitMetalFutureSecurity(final MetalFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      @Override
      public Void visitStockFutureSecurity(final StockFutureSecurity security) {
        return visitFutureSecurity(security);
      }

      private Void visitFutureSecurity(final FutureSecurity security) {
        security.accept(new FinancialSecurityVisitorAdapter<Void>() {

          @Override
          public Void visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitBondFutureSecurity(final BondFutureSecurity security) {
            assertTrue("Security is instance of: " + fromSecMaster.getClass().getName(), fromSecMaster instanceof BondFutureSecurity);
            final BondFutureSecurity actual = (BondFutureSecurity) fromSecMaster;

            assertEquals(new HashSet<>(security.getBasket()), new HashSet<>(actual.getBasket()));

            assertEquals(security.getContractCategory(), actual.getContractCategory());
            assertEquals(security.getCurrency(), actual.getCurrency());
            assertEquals(security.getExpiry(), actual.getExpiry());
            assertEquals(security.getFirstDeliveryDate(), actual.getFirstDeliveryDate());
            assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
            assertEquals(security.getLastDeliveryDate(), actual.getLastDeliveryDate());
            assertEquals(security.getName(), actual.getName());
            assertEquals(security.getSecurityType(), actual.getSecurityType());
            assertEquals(security.getSettlementExchange(), actual.getSettlementExchange());
            assertEquals(security.getTradingExchange(), actual.getTradingExchange());
            assertEquals(security.getUnitAmount(), actual.getUnitAmount());
            assertNotNull(actual.getUniqueId());

            // test underlying is loaded as well
            for (final BondFutureDeliverable deliverable : security.getBasket()) {
              final ExternalIdBundle identifiers = deliverable.getIdentifiers();
              assertUnderlyingIsLoaded(identifiers);
            }
            return null;
          }

          @Override
          public Void visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitEquityFutureSecurity(final EquityFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitFXFutureSecurity(final FXFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitIndexFutureSecurity(final IndexFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
            assertTrue(fromSecMaster instanceof InterestRateFutureSecurity);
            final InterestRateFutureSecurity actual = (InterestRateFutureSecurity) fromSecMaster;
            assertEquals(security.getCurrency(), actual.getCurrency());
            assertEquals(security.getExpiry(), actual.getExpiry());
            assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
            assertEquals(security.getName(), actual.getName());
            assertEquals(security.getSecurityType(), actual.getSecurityType());
            assertEquals(security.getSettlementExchange(), actual.getSettlementExchange());
            assertEquals(security.getTradingExchange(), actual.getTradingExchange());
            assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());
            assertEquals(security.getUnitAmount(), actual.getUnitAmount());
            assertNotNull(actual.getUniqueId());
            return null;
          }

          @Override
          public Void visitMetalFutureSecurity(final MetalFutureSecurity security) {
            assertSecurity();
            return null;
          }

          @Override
          public Void visitStockFutureSecurity(final StockFutureSecurity security) {
            assertSecurity();
            return null;
          }
        });
        return null;
      }

      @Override
      public Void visitSwapSecurity(final SwapSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
        assertTrue(fromSecMaster instanceof EquityIndexOptionSecurity);
        final EquityIndexOptionSecurity actual = (EquityIndexOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());
        assertNotNull(actual.getUniqueId());
        return null;
      }

      @Override
      public Void visitEquityOptionSecurity(final EquityOptionSecurity security) {
        assertTrue(fromSecMaster instanceof EquityOptionSecurity);
        final EquityOptionSecurity actual = (EquityOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());
        assertNotNull(actual.getUniqueId());

        // test underlying is loaded as well
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;
      }

      @Override
      public Void visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitFXOptionSecurity(final FXOptionSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitSwaptionSecurity(final SwaptionSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
        assertTrue(fromSecMaster instanceof IRFutureOptionSecurity);
        final IRFutureOptionSecurity actual = (IRFutureOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.isMargined(), actual.isMargined());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());

        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertNotNull(actual.getUniqueId());

        // test underlying is loaded as well
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;
      }

      @Override
      public Void visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
        assertTrue(fromSecMaster instanceof CommodityFutureOptionSecurity);
        final CommodityFutureOptionSecurity actual = (CommodityFutureOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getTradingExchange(), actual.getTradingExchange());
        assertEquals(security.getSettlementExchange(), actual.getSettlementExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());

        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertNotNull(actual.getUniqueId());

        // test underlying is loaded as well
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;
      }

      @Override
      public Void visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
        assertTrue(fromSecMaster instanceof FxFutureOptionSecurity);
        final FxFutureOptionSecurity actual = (FxFutureOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getTradingExchange(), actual.getTradingExchange());
        assertEquals(security.getSettlementExchange(), actual.getSettlementExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());

        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertNotNull(actual.getUniqueId());

        // test underlying is loaded as well
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;
      }

      @Override
      public Void visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {

        assertTrue(fromSecMaster instanceof EquityIndexDividendFutureOptionSecurity);
        final EquityIndexDividendFutureOptionSecurity actual = (EquityIndexDividendFutureOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.isMargined(), actual.isMargined());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());

        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertNotNull(actual.getUniqueId());

        // test underlying is loaded as well
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;
      }

      @Override
      public Void visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {

        assertTrue(fromSecMaster instanceof EquityIndexFutureOptionSecurity);
        final EquityIndexFutureOptionSecurity actual = (EquityIndexFutureOptionSecurity) fromSecMaster;

        assertEquals(security.getCurrency(), actual.getCurrency());

        assertEquals(security.getExchange(), actual.getExchange());
        assertEquals(security.getExerciseType(), actual.getExerciseType());
        assertEquals(security.getExpiry(), actual.getExpiry());
        assertEquals(security.getOptionType(), actual.getOptionType());
        assertEquals(security.getPointValue(), actual.getPointValue());
        assertEquals(security.getStrike(), actual.getStrike());
        assertEquals(security.isMargined(), actual.isMargined());
        assertEquals(security.getUnderlyingId(), actual.getUnderlyingId());

        assertEquals(security.getExternalIdBundle(), actual.getExternalIdBundle());
        assertEquals(security.getName(), actual.getName());
        assertEquals(security.getSecurityType(), actual.getSecurityType());
        assertNotNull(actual.getUniqueId());

        // test underlying is loaded as well
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        assertUnderlyingIsLoaded(underlyingIdentifier);
        return null;
      }

      @Override
      public Void visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitFXForwardSecurity(final FXForwardSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitCapFloorSecurity(final CapFloorSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
        assertSecurity();
        return null;
      }

      @Override
      public Void visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
        assertSecurity();
        return null;
      }
    });
  }

  public void testEquityOptionSecurity() {
    assertLoadAndSaveSecurity(makeAPVLEquityOptionSecurity());
  }

  public void testEquityIndexOptionSecurity() {
    assertLoadAndSaveSecurity(makeSPXIndexOptionSecurity());
  }

  public void testEquityIndexFutureOptionSecurity() {
    assertLoadAndSaveSecurity(makeEquityIndexFutureOptionSecurity());
  }

  public void testEquityIndexDividendFutureOptionSecurity() {
    assertLoadAndSaveSecurity(makeEquityIndexDividendFutureOptionSecurity());
  }

  public void testEquitySecurity() {
    assertLoadAndSaveSecurity(makeExpectedAAPLEquitySecurity());
  }

  public void testInterestRateFutureSecurity() {
    assertLoadAndSaveSecurity(makeInterestRateFuture());
  }

  public void testBondFutureSecurity() {
    assertLoadAndSaveSecurity(makeUSBondFuture());
  }

  public void testIRFutureOptionSecurity() {
    assertLoadAndSaveSecurity(makeEURODOLLARFutureOptionSecurity());
    assertLoadAndSaveSecurity(makeLIBORFutureOptionSecurity());
    assertLoadAndSaveSecurity(makeEURIBORFutureOptionSecurity());
  }

  public void testCommodityFutureOptionSecurity() {
    assertLoadAndSaveSecurity(makeCommodityFutureOptionSecurity());
  }

  public void testFxFutureOptionSecurity() {
    assertLoadAndSaveSecurity(makeFxFutureOptionSecurity());
  }

  private void assertUnderlyingIsLoaded(final ExternalId underlyingIdentifier) {
    assertUnderlyingIsLoaded(ExternalIdBundle.of(underlyingIdentifier));
  }

  private void assertUnderlyingIsLoaded(final ExternalIdBundle identifiers) {
    final SecuritySearchResult result = _securityMaster.search(new SecuritySearchRequest(identifiers));
    assertNotNull(result);
    assertFalse(result.getDocuments().isEmpty());
    assertNotNull(result.getFirstDocument().getSecurity());
  }

}
