/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.exposure.factory;

import static com.opengamma.financial.testutils.SecurityInstances.BILL;
import static com.opengamma.financial.testutils.SecurityInstances.CASH;
import static com.opengamma.financial.testutils.SecurityInstances.CONTINUOUS_ZERO_DEPOSIT;
import static com.opengamma.financial.testutils.SecurityInstances.CORPORATE_BOND;
import static com.opengamma.financial.testutils.SecurityInstances.CROSS_CURRENCY_SWAP;
import static com.opengamma.financial.testutils.SecurityInstances.CROSS_CURRENCY_SWAPTION;
import static com.opengamma.financial.testutils.SecurityInstances.CROSS_CURRENCY_SWAP_ID;
import static com.opengamma.financial.testutils.SecurityInstances.EQUITY_VARIANCE_SWAP;
import static com.opengamma.financial.testutils.SecurityInstances.FRA;
import static com.opengamma.financial.testutils.SecurityInstances.GOVERNMENT_BOND;
import static com.opengamma.financial.testutils.SecurityInstances.INFLATION_BOND;
import static com.opengamma.financial.testutils.SecurityInstances.MUNICIPAL_BOND;
import static com.opengamma.financial.testutils.SecurityInstances.VANILLA_IBOR_SWAP;
import static com.opengamma.financial.testutils.SecurityInstances.VANILLA_IBOR_SWAPTION;
import static com.opengamma.financial.testutils.SecurityInstances.VANILLA_IBOR_SWAP_ID;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Unit tests for {@link SecurityCurrencyAndRegionExposureFunction}.
 */
public class SecurityCurrencyAndRegionExposureFunctionTest {
  /** The compilation context */
  private static final FunctionCompilationContext COMPILATION_CONTEXT = new FunctionCompilationContext();
  /** The execution context */
  private static final FunctionExecutionContext EXECUTION_CONTEXT = new FunctionExecutionContext();
  /** The exposure function */
  private static final SecurityCurrencyAndRegionExposureFunction EXPOSURE_FUNCTION = new SecurityCurrencyAndRegionExposureFunction();

  static {
    COMPILATION_CONTEXT.setSecuritySource(DummySecuritySource.getInstance());
    EXECUTION_CONTEXT.setSecuritySource(DummySecuritySource.getInstance());
  }

  /**
   * Tests the behaviour when a security source is not available.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNoSecuritySource() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(CASH));
    EXPOSURE_FUNCTION.getIds(trade);
  }

  /**
   * Tests the ids returned for a cash security.
   */
  @Test
  public void testCashSecurity() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(CASH));
    final List<ExternalId> expected = Collections.singletonList(ExternalId.of("SecurityType", "CASH_AUD_AU"));
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, COMPILATION_CONTEXT), expected);
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, EXECUTION_CONTEXT), expected);
  }

  /**
   * Tests the ids returned for a continuous zero deposit security.
   */
  @Test
  public void testContinuousZeroDepositSecurity() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(CONTINUOUS_ZERO_DEPOSIT));
    final List<ExternalId> expected = Collections.singletonList(ExternalId.of("SecurityType", "CONTINUOUS_ZERO_DEPOSIT_BRL_BR"));
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, COMPILATION_CONTEXT), expected);
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, EXECUTION_CONTEXT), expected);
  }

  /**
   * Tests the ids returned for a corporate bond security.
   */
  @Test
  public void testCorporateBondSecurity() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(CORPORATE_BOND));
    final List<ExternalId> expected = Collections.singletonList(ExternalId.of("SecurityType", "BOND_EUR_ES"));
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, COMPILATION_CONTEXT), expected);
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, EXECUTION_CONTEXT), expected);
  }

  /**
   * Tests the ids returned for an equity variance swap security.
   */
  @Test
  public void testEquityVarianceSwapSecurity() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(EQUITY_VARIANCE_SWAP));
    final List<ExternalId> expected = Collections.singletonList(ExternalId.of("SecurityType", "EQUITY VARIANCE SWAP_GBP_GB"));
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, COMPILATION_CONTEXT), expected);
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, EXECUTION_CONTEXT), expected);
  }

  /**
   * Tests the ids returned for a corporate bond security.
   */
  @Test
  public void testFraSecurity() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(FRA));
    final List<ExternalId> expected = Collections.singletonList(ExternalId.of("SecurityType", "FRA_CAD_CA"));
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, COMPILATION_CONTEXT), expected);
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, EXECUTION_CONTEXT), expected);
  }

  /**
   * Tests the ids returned for a government bond security.
   */
  @Test
  public void testGovernmentBondSecurity() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(GOVERNMENT_BOND));
    final List<ExternalId> expected = Collections.singletonList(ExternalId.of("SecurityType", "BOND_EUR_ES"));
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, COMPILATION_CONTEXT), expected);
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, EXECUTION_CONTEXT), expected);
  }

  /**
   * Tests the ids returned for an inflation bond security.
   */
  @Test
  public void testInflationBondSecurity() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(INFLATION_BOND));
    final List<ExternalId> expected = Collections.singletonList(ExternalId.of("SecurityType", "BOND_EUR_ES"));
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, COMPILATION_CONTEXT), expected);
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, EXECUTION_CONTEXT), expected);
  }

  /**
   * Tests the ids returned for a municipal bond security.
   */
  @Test
  public void testMunicipalBondSecurity() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(MUNICIPAL_BOND));
    final List<ExternalId> expected = Collections.singletonList(ExternalId.of("SecurityType", "BOND_EUR_ES"));
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, COMPILATION_CONTEXT), expected);
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, EXECUTION_CONTEXT), expected);
  }


  /**
   * Tests the ids returned for a vanilla ibor swap security.
   */
  @Test
  public void testVanillaIborSwapSecurity() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(VANILLA_IBOR_SWAP));
    final List<ExternalId> expected = Collections.singletonList(ExternalId.of("SecurityType", "SWAP_USD_US"));
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, COMPILATION_CONTEXT), expected);
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, EXECUTION_CONTEXT), expected);
  }

  /**
   * Tests the ids returned for a cross-currency swap security.
   */
  @Test
  public void testCrossCurrencySwapSecurity() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(CROSS_CURRENCY_SWAP));
    final List<ExternalId> expected = Arrays.asList(ExternalId.of("SecurityType", "SWAP_USD_US"), ExternalId.of("SecurityType", "SWAP_MXN_MX"));
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, COMPILATION_CONTEXT), expected);
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, EXECUTION_CONTEXT), expected);
  }

  /**
   * Tests the ids returned for a bill security.
   */
  @Test
  public void testBillSecurity() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(BILL));
    final List<ExternalId> expected = Collections.singletonList(ExternalId.of("SecurityType", "BILL_NZD_NZ"));
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, COMPILATION_CONTEXT), expected);
    assertEquals(EXPOSURE_FUNCTION.getIds(trade, EXECUTION_CONTEXT), expected);
  }

  /**
   * Tests the ids returned for a swaption security.
   */
  @Test
  public void testSwaptionSecurity() {
    final SecuritySource source = new SecuritySource() {

      @Override
      public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Collection<Security> get(final ExternalIdBundle bundle) {
        return null;
      }

      @Override
      public Security getSingle(final ExternalIdBundle bundle) {
        if (bundle.equals(VANILLA_IBOR_SWAP_ID.toBundle())) {
          return VANILLA_IBOR_SWAP;
        } else if (bundle.equals(CROSS_CURRENCY_SWAP_ID.toBundle())) {
          return CROSS_CURRENCY_SWAP;
        }
        return null;
      }

      @Override
      public Security getSingle(final ExternalIdBundle bundle,
          final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Security get(final UniqueId uniqueId) {
        return null;
      }

      @Override
      public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
        return null;
      }

      @Override
      public Map<ObjectId, Security> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public ChangeManager changeManager() {
        return null;
      }

    };
    final FunctionCompilationContext compilationContext = new FunctionCompilationContext();
    compilationContext.setSecuritySource(source);
    final FunctionExecutionContext executionContext = new FunctionExecutionContext();
    executionContext.setSecuritySource(source);
    final SecurityCurrencyAndRegionExposureFunction exposureFunction = new SecurityCurrencyAndRegionExposureFunction();
    final SimpleTrade trade = new SimpleTrade();
    trade.setSecurityLink(SimpleSecurityLink.of(VANILLA_IBOR_SWAPTION));
    List<ExternalId> expected = Collections.singletonList(ExternalId.of("SecurityType", "SWAPTION_USD_US"));
    assertEquals(exposureFunction.getIds(trade, compilationContext), expected);
    assertEquals(exposureFunction.getIds(trade, executionContext), expected);
    trade.setSecurityLink(SimpleSecurityLink.of(CROSS_CURRENCY_SWAPTION));
    expected = Arrays.asList(ExternalId.of("SecurityType", "SWAPTION_USD_US"), ExternalId.of("SecurityType", "SWAPTION_MXN_MX"));
    assertEquals(exposureFunction.getIds(trade, compilationContext), expected);
    assertEquals(exposureFunction.getIds(trade, executionContext), expected);
  }
}
