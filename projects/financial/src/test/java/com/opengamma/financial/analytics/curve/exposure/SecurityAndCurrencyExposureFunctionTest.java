/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
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
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test for {@link SecurityAndCurrencyExposureFunction}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityAndCurrencyExposureFunctionTest {
  private static final String SCHEME = ExposureFunction.SECURITY_IDENTIFIER;
  private static final ExposureFunction EXPOSURE_FUNCTION = new SecurityAndCurrencyExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));

  @Test
  public void testAgriculturalFutureSecurity() {
    final AgricultureFutureSecurity future = ExposureFunctionTestHelper.getAgricultureFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testBondFutureSecurity() {
    final BondFutureSecurity future = ExposureFunctionTestHelper.getBondFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_EUR"), ids.get(0));
  }

  @Test
  public void testCashSecurity() {
    final CashSecurity cash = ExposureFunctionTestHelper.getCashSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(cash);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "CASH_USD"), ids.get(0));
  }

  @Test
  public void testCapFloorCMSSpreadSecurity() {
    final CapFloorCMSSpreadSecurity security = ExposureFunctionTestHelper.getCapFloorCMSSpreadSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "CAP-FLOOR CMS SPREAD_EUR"), ids.get(0));
  }

  @Test
  public void testCapFloorSecurity() {
    final CapFloorSecurity security = ExposureFunctionTestHelper.getCapFloorSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "CAP-FLOOR_USD"), ids.get(0));
  }

  @Test
  public void testContinuousZeroDepositSecurity() {
    final ContinuousZeroDepositSecurity security = ExposureFunctionTestHelper.getContinuousZeroDepositSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "CONTINUOUS_ZERO_DEPOSIT_EUR"), ids.get(0));
  }

  @Test
  public void testCorporateBondSecurity() {
    final CorporateBondSecurity security = ExposureFunctionTestHelper.getCorporateBondSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "BOND_USD"), ids.get(0));
  }

  @Test
  public void testEnergyFutureSecurity() {
    final EnergyFutureSecurity future = ExposureFunctionTestHelper.getEnergyFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testEquityFutureSecurity() {
    final EquityFutureSecurity future = ExposureFunctionTestHelper.getEquityFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testEquityIndexDividendFutureSecurity() {
    final EquityIndexDividendFutureSecurity future = ExposureFunctionTestHelper.getEquityIndexDividendFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testFRASecurity() {
    final FRASecurity fra = ExposureFunctionTestHelper.getFRASecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(fra);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FRA_USD"), ids.get(0));
  }

  @Test
  public void testFXFutureSecurity() {
    final FXFutureSecurity future = ExposureFunctionTestHelper.getFXFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(2, ids.size());
    assertTrue(ids.containsAll(Arrays.asList(ExternalId.of(SCHEME, "FUTURE_EUR"), ExternalId.of(SCHEME, "FUTURE_USD"))));
  }

  @Test
  public void testIndexFutureSecurity() {
    final IndexFutureSecurity future = ExposureFunctionTestHelper.getIndexFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_EUR"), ids.get(0));
  }

  @Test
  public void testInterestRateFutureSecurity() {
    final InterestRateFutureSecurity future = ExposureFunctionTestHelper.getInterestRateFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testFederalFundsFutureSecurity() {
    final FederalFundsFutureSecurity future = ExposureFunctionTestHelper.getFederalFundsFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testMetalFutureSecurity() {
    final MetalFutureSecurity future = ExposureFunctionTestHelper.getMetalFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testStockFutureSecurity() {
    final StockFutureSecurity future = ExposureFunctionTestHelper.getStockFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testAgricultureFutureSecurity() {
    final AgricultureFutureSecurity future = ExposureFunctionTestHelper.getAgricultureFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_USD"), ids.get(0));
  }

  @Test
  public void testBondFutureOptionSecurity() {
    final BondFutureOptionSecurity security = ExposureFunctionTestHelper.getBondFutureOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "BONDFUTURE_OPTION_EUR"), ids.get(0));
  }

  @Test
  public void testCashFlowSecurity() {
    final CashFlowSecurity security = ExposureFunctionTestHelper.getCashFlowSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "CASHFLOW_EUR"), ids.get(0));
  }

  @Test
  public void testEnergyFutureOptionSecurity() {
    final CommodityFutureOptionSecurity security = ExposureFunctionTestHelper.getEnergyFutureOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "COMMODITYFUTURE_OPTION_EUR"), ids.get(0));
  }

  @Test
  public void testEquityBarrierOptionSecurity() {
    final EquityBarrierOptionSecurity security = ExposureFunctionTestHelper.getEquityBarrierOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "EQUITY_BARRIER_OPTION_EUR"), ids.get(0));
  }

  @Test
  public void testEquitySecurity() {
    final EquitySecurity security = ExposureFunctionTestHelper.getEquitySecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "EQUITY_USD"), ids.get(0));
  }

  @Test
  public void testAgricultureForwardSecurity() {
    final AgricultureForwardSecurity security = ExposureFunctionTestHelper.getAgricultureForwardSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "COMMODITY_FORWARD_USD"), ids.get(0));
  }

  @Test
  public void testCreditDefaultSwapIndexSecurity() {
    final CreditDefaultSwapIndexSecurity security = ExposureFunctionTestHelper.getCreditDefaultSwapIndexSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "CDS_INDEX_EUR"), ids.get(0));
  }

  @Test
  public void testCreditDefaultSwapOptionSecurity() {
    final CreditDefaultSwapOptionSecurity security = ExposureFunctionTestHelper.getCreditDefaultSwapOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "CREDIT_DEFAULT_SWAP_OPTION_USD"), ids.get(0));
  }

  @Test
  public void testDeliverableSwapSecurity() {
    final DeliverableSwapFutureSecurity security = ExposureFunctionTestHelper.getDeliverableSwapFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FUTURE_EUR"), ids.get(0));
  }

  @Test
  public void testEnergyForwardSecurity() {
    final EnergyForwardSecurity security = ExposureFunctionTestHelper.getEnergyForwardSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "COMMODITY_FORWARD_USD"), ids.get(0));
  }

  @Test
  public void testEquityIndexDividendFutureOptionSecurity() {
    final EquityIndexDividendFutureOptionSecurity security = ExposureFunctionTestHelper.getEquityIndexDividendFutureOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "EQUITY_INDEX_DIVIDEND_FUTURE_OPTION_USD"), ids.get(0));
  }

  @Test
  public void testEquityIndexFutureOptionSecurity() {
    final EquityIndexFutureOptionSecurity security = ExposureFunctionTestHelper.getEquityIndexFutureOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "EQUITY_INDEX_FUTURE_OPTION_USD"), ids.get(0));
  }

  @Test
  public void testEquityIndexOptionSecurity() {
    final EquityIndexOptionSecurity security = ExposureFunctionTestHelper.getEquityIndexOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "EQUITY_INDEX_OPTION_EUR"), ids.get(0));
  }

  @Test
  public void testEquityOptionSecurity() {
    final EquityOptionSecurity security = ExposureFunctionTestHelper.getEquityOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "EQUITY_OPTION_EUR"), ids.get(0));
  }

  @Test
  public void testEquityVarianceSwapSecurity() {
    final EquityVarianceSwapSecurity security = ExposureFunctionTestHelper.getEquityVarianceSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "EQUITY VARIANCE SWAP_USD"), ids.get(0));
  }

  @Test
  public void testFixedFloatSwapSecurity() {
    final SwapSecurity security = ExposureFunctionTestHelper.getPayFixedFloatSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "SWAP_EUR"), ids.get(0));
  }

  @Test
  public void testFloatFloatSwapSecurity() {
    final SwapSecurity security = ExposureFunctionTestHelper.getFloatFloatSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "SWAP_EUR"), ids.get(0));
  }

  @Test
  public void testForwardFixedFloatSwapSecurity() {
    final ForwardSwapSecurity security = ExposureFunctionTestHelper.getPayForwardFixedFloatSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "SWAP_EUR"), ids.get(0));
  }

  @Test
  public void testForwardFloatFloatSwapSecurity() {
    final ForwardSwapSecurity security = ExposureFunctionTestHelper.getForwardFloatFloatSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "SWAP_EUR"), ids.get(0));
  }

  @Test
  public void testForwardXCcySwapSecurity() {
    final ForwardSwapSecurity security = ExposureFunctionTestHelper.getForwardXCcySwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(2, ids.size());
    assertTrue(ids.containsAll(Arrays.asList(ExternalId.of(SCHEME, "SWAP_USD"), ExternalId.of(SCHEME, "SWAP_EUR"))));
  }

  @Test
  public void testFXBarrierOptionSecurity() {
    final FXBarrierOptionSecurity security = ExposureFunctionTestHelper.getFXBarrierOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(2, ids.size());
    assertTrue(ids.containsAll(Arrays.asList(ExternalId.of(SCHEME, "FX_BARRIER_OPTION_USD"), ExternalId.of(SCHEME, "FX_BARRIER_OPTION_EUR"))));
  }

  @Test
  public void testFXDigitalOptionSecurity() {
    final FXDigitalOptionSecurity security = ExposureFunctionTestHelper.getFXDigitalOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(2, ids.size());
    assertTrue(ids.containsAll(Arrays.asList(ExternalId.of(SCHEME, "FX_DIGITAL_OPTION_USD"), ExternalId.of(SCHEME, "FX_DIGITAL_OPTION_EUR"))));
  }

  @Test
  public void testFXForwardSecurity() {
    final FXForwardSecurity security = ExposureFunctionTestHelper.getFXForwardSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(2, ids.size());
    assertTrue(ids.containsAll(Arrays.asList(ExternalId.of(SCHEME, "FX_FORWARD_USD"), ExternalId.of(SCHEME, "FX_FORWARD_EUR"))));
  }

  @Test
  public void testFXFutureOptionSecurity() {
    final FxFutureOptionSecurity security = ExposureFunctionTestHelper.getFXFutureOptionSecurity();
    final FXFutureSecurity underlying = ExposureFunctionTestHelper.getFXFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = new SecurityAndCurrencyExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying)).getIds(trade);
    assertEquals(2, ids.size());
    assertTrue(ids.containsAll(Arrays.asList(ExternalId.of(SCHEME, "FXFUTURE_OPTION_USD"), ExternalId.of(SCHEME, "FXFUTURE_OPTION_EUR"))));
  }

  @Test
  public void testFXOptionSecurity() {
    final FXOptionSecurity security = ExposureFunctionTestHelper.getFXOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(2, ids.size());
    assertTrue(ids.containsAll(Arrays.asList(ExternalId.of(SCHEME, "FX_OPTION_USD"), ExternalId.of(SCHEME, "FX_OPTION_EUR"))));
  }

  @Test
  public void testFXVolatilitySecurity() {
    final FXVolatilitySwapSecurity security = ExposureFunctionTestHelper.getFXVolatilitySwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "FX_VOLATILITY_SWAP_USD"), ids.get(0));
  }

  @Test
  public void testGovernmentBondSecurity() {
    final GovernmentBondSecurity security = ExposureFunctionTestHelper.getGovernmentBondSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "BOND_USD"), ids.get(0));
  }

  @Test
  public void testInterestRateFutureOptionSecurity() {
    final IRFutureOptionSecurity security = ExposureFunctionTestHelper.getInterestRateFutureOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "IRFUTURE_OPTION_EUR"), ids.get(0));
  }

  @Test
  public void testLegacyFixedRecoveryCDSSecurity() {
    final LegacyFixedRecoveryCDSSecurity security = ExposureFunctionTestHelper.getLegacyFixedRecoveryCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "LEGACY_FIXED_RECOVERY_CDS_EUR"), ids.get(0));
  }

  @Test
  public void testLegacyRecoveryLockCDSSecurity() {
    final LegacyRecoveryLockCDSSecurity security = ExposureFunctionTestHelper.getLegacyRecoveryLockCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "LEGACY_RECOVERY_LOCK_CDS_EUR"), ids.get(0));
  }

  @Test
  public void testLegacyVanillaCDSSecurity() {
    final LegacyVanillaCDSSecurity security = ExposureFunctionTestHelper.getLegacyVanillaCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "LEGACY_VANILLA_CDS_EUR"), ids.get(0));
  }

  @Test
  public void testMetalForwardSecurity() {
    final MetalForwardSecurity security = ExposureFunctionTestHelper.getMetalForwardSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "COMMODITY_FORWARD_USD"), ids.get(0));
  }

  @Test
  public void testMunicipalBondSecurity() {
    final MunicipalBondSecurity security = ExposureFunctionTestHelper.getMunicipalBondSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "BOND_USD"), ids.get(0));
  }

  @Test
  public void testNonDeliverableFXDigitalOptionSecurity() {
    final NonDeliverableFXDigitalOptionSecurity security = ExposureFunctionTestHelper.getNonDeliverableFXDigitalOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(2, ids.size());
    assertTrue(ids.containsAll(
        Arrays.asList(ExternalId.of(SCHEME, "NONDELIVERABLE_FX_DIGITAL_OPTION_USD"), ExternalId.of(SCHEME, "NONDELIVERABLE_FX_DIGITAL_OPTION_EUR"))));
  }

  @Test
  public void testNonDeliverableFXForwardSecurity() {
    final NonDeliverableFXForwardSecurity security = ExposureFunctionTestHelper.getNonDeliverableFXForwardSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(2, ids.size());
    assertTrue(ids.containsAll(Arrays.asList(ExternalId.of(SCHEME, "NONDELIVERABLE_FX_FORWARD_USD"), ExternalId.of(SCHEME, "NONDELIVERABLE_FX_FORWARD_EUR"))));
  }

  @Test
  public void testNonDeliverableFXOptionSecurity() {
    final NonDeliverableFXOptionSecurity security = ExposureFunctionTestHelper.getNonDeliverableFXOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(2, ids.size());
    assertTrue(ids.containsAll(Arrays.asList(ExternalId.of(SCHEME, "NONDELIVERABLE_FX_OPTION_USD"), ExternalId.of(SCHEME, "NONDELIVERABLE_FX_OPTION_EUR"))));
  }

  @Test
  public void testPeriodicZeroDepositSecurity() {
    final PeriodicZeroDepositSecurity security = ExposureFunctionTestHelper.getPeriodicZeroDepositSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "PERIODIC_ZERO_DEPOSIT_USD"), ids.get(0));
  }

  @Test
  public void testSimpleZeroDepositSecurity() {
    final SimpleZeroDepositSecurity security = ExposureFunctionTestHelper.getSimpleZeroDepositSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "SIMPLE_ZERO_DEPOSIT_USD"), ids.get(0));
  }

  @Test
  public void testStandardFixedRecoveryCDSSecurity() {
    final StandardFixedRecoveryCDSSecurity security = ExposureFunctionTestHelper.getStandardFixedRecoveryCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "STANDARD_FIXED_RECOVERY_CDS_EUR"), ids.get(0));
  }

  @Test
  public void testStandardRecoveryLockCDSSecurity() {
    final StandardRecoveryLockCDSSecurity security = ExposureFunctionTestHelper.getStandardRecoveryLockCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "STANDARD_RECOVERY_LOCK_CDS_EUR"), ids.get(0));
  }

  @Test
  public void testStandardVanillaCDSSecurity() {
    final StandardVanillaCDSSecurity security = ExposureFunctionTestHelper.getStandardVanillaCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "STANDARD_VANILLA_CDS_EUR"), ids.get(0));
  }

  @Test
  public void testSwaptionSecurity() {
    final SwaptionSecurity security = ExposureFunctionTestHelper.getPaySwaptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "SWAPTION_EUR"), ids.get(0));
  }

  @Test
  public void testXCcySwapSecurity() {
    final SwapSecurity security = ExposureFunctionTestHelper.getXCcySwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(2, ids.size());
    assertTrue(ids.containsAll(Arrays.asList(ExternalId.of(SCHEME, "SWAP_USD"), ExternalId.of(SCHEME, "SWAP_EUR"))));
  }

  @Test
  public void testPayYoYInflationSwapSecurity() {
    final YearOnYearInflationSwapSecurity security = ExposureFunctionTestHelper.getPayYoYInflationSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "YEAR_ON_YEAR_INFLATION_SWAP_USD"), ids.get(0));
  }

  @Test
  public void testReceiveYoYInflationSwapSecurity() {
    final YearOnYearInflationSwapSecurity security = ExposureFunctionTestHelper.getPayYoYInflationSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "YEAR_ON_YEAR_INFLATION_SWAP_USD"), ids.get(0));
  }

  @Test
  public void testPayZeroCouponInflationSwapSecurity() {
    final ZeroCouponInflationSwapSecurity security = ExposureFunctionTestHelper.getPayZeroCouponInflationSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "ZERO_COUPON_INFLATION_SWAP_USD"), ids.get(0));
  }

  @Test
  public void testReceiveZeroCouponInflationSwapSecurity() {
    final ZeroCouponInflationSwapSecurity security = ExposureFunctionTestHelper.getReceiveZeroCouponInflationSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(SCHEME, "ZERO_COUPON_INFLATION_SWAP_USD"), ids.get(0));
  }
}
