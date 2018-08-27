/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

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
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
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
 * Unit test for ContractCategoryExposureFunction.
 */
@Test(groups = TestGroup.UNIT)
public class ContractCategoryExposureFunctionTest {

  private static final ExposureFunction EXPOSURE_FUNCTION = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(null));

  @Test
  public void testAgricultureForwardSecurity() {
    final AgricultureForwardSecurity security = ExposureFunctionTestHelper.getAgricultureForwardSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Commodity"), ids.get(0));
  }

  @Test
  public void testAgriculturalFutureSecurity() {
    final AgricultureFutureSecurity future = ExposureFunctionTestHelper.getAgricultureFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Commodity"), ids.get(0));
  }

  @Test
  public void testBondFutureOptionSecurity() {
    final BondFutureSecurity underlying = ExposureFunctionTestHelper.getBondFutureSecurity();
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying));
    final BondFutureOptionSecurity security = ExposureFunctionTestHelper.getBondFutureOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = exposureFunction.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Financial"), ids.get(0));
  }

  @Test
  public void testBondFutureSecurity() {
    final BondFutureSecurity future = ExposureFunctionTestHelper.getBondFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Financial"), ids.get(0));
  }

  @Test
  public void testCapFloorCMSSpreadSecurity() {
    final CapFloorCMSSpreadSecurity security = ExposureFunctionTestHelper.getCapFloorCMSSpreadSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testCapFloorSecurity() {
    final CapFloorSecurity security = ExposureFunctionTestHelper.getCapFloorSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testCashFlowSecurity() {
    final CashFlowSecurity security = ExposureFunctionTestHelper.getCashFlowSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testCashSecurity() {
    final CashSecurity cash = ExposureFunctionTestHelper.getCashSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(cash);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testContinuousZeroDepositSecurity() {
    final ContinuousZeroDepositSecurity security = ExposureFunctionTestHelper.getContinuousZeroDepositSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testCorporateBondSecurity() {
    final CorporateBondSecurity security = ExposureFunctionTestHelper.getCorporateBondSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testCreditDefaultSwapIndexSecurity() {
    final CreditDefaultSwapIndexDefinitionSecurity underlying = ExposureFunctionTestHelper.getCreditDefaultSwapIndexDefinitionSecurity();
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying));
    final CreditDefaultSwapIndexSecurity security = ExposureFunctionTestHelper.getCreditDefaultSwapIndexSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = exposureFunction.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testCreditDefaultSwapOptionSecurity() {
    final StandardVanillaCDSSecurity underlying = ExposureFunctionTestHelper.getStandardVanillaCDSSecurity();
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying));
    final CreditDefaultSwapOptionSecurity security = ExposureFunctionTestHelper.getCreditDefaultSwapOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = exposureFunction.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testDeliverableSwapSecurity() {
    final DeliverableSwapFutureSecurity security = ExposureFunctionTestHelper.getDeliverableSwapFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testEnergyForwardSecurity() {
    final EnergyForwardSecurity security = ExposureFunctionTestHelper.getEnergyForwardSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Energy"), ids.get(0));
  }

  @Test
  public void testEnergyFutureOptionSecurity() {
    final EnergyFutureSecurity underlying = ExposureFunctionTestHelper.getEnergyFutureSecurity();
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying));
    final CommodityFutureOptionSecurity security = ExposureFunctionTestHelper.getEnergyFutureOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = exposureFunction.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Energy"), ids.get(0));
  }

  @Test
  public void testEnergyFutureSecurity() {
    final EnergyFutureSecurity future = ExposureFunctionTestHelper.getEnergyFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Energy"), ids.get(0));
  }

  @Test
  public void testEquityBarrierOptionSecurity() {
    final EquitySecurity underlying = ExposureFunctionTestHelper.getEquitySecurity();
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying));
    final EquityBarrierOptionSecurity security = ExposureFunctionTestHelper.getEquityBarrierOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = exposureFunction.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testEquityFutureSecurity() {
    final EquityFutureSecurity future = ExposureFunctionTestHelper.getEquityFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Equity"), ids.get(0));
  }

  @Test
  public void testEquityIndexDividendFutureOptionSecurity() {
    final EquityIndexDividendFutureSecurity underlying = ExposureFunctionTestHelper.getEquityIndexDividendFutureSecurity();
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying));
    final EquityIndexDividendFutureOptionSecurity security = ExposureFunctionTestHelper.getEquityIndexDividendFutureOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = exposureFunction.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Equity Index"), ids.get(0));
  }

  @Test
  public void testEquityIndexDividendFutureSecurity() {
    final EquityIndexDividendFutureSecurity future = ExposureFunctionTestHelper.getEquityIndexDividendFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Equity Index"), ids.get(0));
  }

  @Test
  public void testEquityIndexFutureOptionSecurity() {
    final EquityFutureSecurity underlying = ExposureFunctionTestHelper.getEquityFutureSecurity();
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying));
    final EquityIndexFutureOptionSecurity security = ExposureFunctionTestHelper.getEquityIndexFutureOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = exposureFunction.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Equity"), ids.get(0));
  }

  @Test
  public void testEquityIndexOptionSecurity() {
    final EquitySecurity underlying = ExposureFunctionTestHelper.getEquitySecurity();
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying));
    final EquityIndexOptionSecurity security = ExposureFunctionTestHelper.getEquityIndexOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = exposureFunction.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testEquityOptionSecurity() {
    final EquitySecurity underlying = ExposureFunctionTestHelper.getEquitySecurity();
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying));
    final EquityOptionSecurity security = ExposureFunctionTestHelper.getEquityOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = exposureFunction.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testEquitySecurity() {
    final EquitySecurity security = ExposureFunctionTestHelper.getEquitySecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testEquityVarianceSwapSecurity() {
    final EquityVarianceSwapSecurity security = ExposureFunctionTestHelper.getEquityVarianceSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testFixedFloatSwapSecurity() {
    final SwapSecurity security = ExposureFunctionTestHelper.getPayFixedFloatSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testFloatFloatSwapSecurity() {
    final SwapSecurity security = ExposureFunctionTestHelper.getFloatFloatSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testFRASecurity() {
    final FRASecurity fra = ExposureFunctionTestHelper.getFRASecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(fra);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testForwardFixedFloatSwapSecurity() {
    final ForwardSwapSecurity security = ExposureFunctionTestHelper.getPayForwardFixedFloatSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testForwardFloatFloatSwapSecurity() {
    final ForwardSwapSecurity security = ExposureFunctionTestHelper.getForwardFloatFloatSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testForwardXCcySwapSecurity() {
    final ForwardSwapSecurity security = ExposureFunctionTestHelper.getForwardXCcySwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testFXBarrierOptionSecurity() {
    final FXBarrierOptionSecurity security = ExposureFunctionTestHelper.getFXBarrierOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testFXDigitalOptionSecurity() {
    final FXDigitalOptionSecurity security = ExposureFunctionTestHelper.getFXDigitalOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testFXForwardSecurity() {
    final FXForwardSecurity security = ExposureFunctionTestHelper.getFXForwardSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testFXFutureOptionSecurity() {
    final FXFutureSecurity underlying = ExposureFunctionTestHelper.getFXFutureSecurity();
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying));
    final FxFutureOptionSecurity security = ExposureFunctionTestHelper.getFXFutureOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = exposureFunction.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Currency"), ids.get(0));
  }

  @Test
  public void testFXFutureSecurity() {
    final FXFutureSecurity future = ExposureFunctionTestHelper.getFXFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Currency"), ids.get(0));
  }

  @Test
  public void testFXVolatilitySecurity() {
    final FXVolatilitySwapSecurity security = ExposureFunctionTestHelper.getFXVolatilitySwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testFXOptionSecurity() {
    final FXOptionSecurity security = ExposureFunctionTestHelper.getFXOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testGovernmentBondSecurity() {
    final GovernmentBondSecurity security = ExposureFunctionTestHelper.getGovernmentBondSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testIndexFutureSecurity() {
    final IndexFutureSecurity future = ExposureFunctionTestHelper.getIndexFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Equity Index"), ids.get(0));
  }

  @Test
  public void testInterestRateFutureOptionSecurity() {
    final InterestRateFutureSecurity underlying = ExposureFunctionTestHelper.getInterestRateFutureSecurity();
    final ExposureFunction exposureFunction = new ContractCategoryExposureFunction(ExposureFunctionTestHelper.getSecuritySource(underlying));
    final IRFutureOptionSecurity security = ExposureFunctionTestHelper.getInterestRateFutureOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = exposureFunction.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Financial"), ids.get(0));
  }

  @Test
  public void testInterestRateFutureSecurity() {
    final InterestRateFutureSecurity future = ExposureFunctionTestHelper.getInterestRateFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Financial"), ids.get(0));
  }

  @Test
  public void testFederalFundsFutureSecurity() {
    final FederalFundsFutureSecurity future = ExposureFunctionTestHelper.getFederalFundsFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Financial"), ids.get(0));
  }

  @Test
  public void testLegacyFixedRecoveryCDSSecurity() {
    final LegacyFixedRecoveryCDSSecurity security = ExposureFunctionTestHelper.getLegacyFixedRecoveryCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testLegacyRecoveryLockCDSSecurity() {
    final LegacyRecoveryLockCDSSecurity security = ExposureFunctionTestHelper.getLegacyRecoveryLockCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testLegacyVanillaCDSSecurity() {
    final LegacyVanillaCDSSecurity security = ExposureFunctionTestHelper.getLegacyVanillaCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testMetalForwardSecurity() {
    final MetalForwardSecurity security = ExposureFunctionTestHelper.getMetalForwardSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Commodity"), ids.get(0));
  }

  @Test
  public void testMetalFutureSecurity() {
    final MetalFutureSecurity future = ExposureFunctionTestHelper.getMetalFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Commodity"), ids.get(0));
  }

  @Test
  public void testMunicipalBondSecurity() {
    final MunicipalBondSecurity security = ExposureFunctionTestHelper.getMunicipalBondSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testNonDeliverableFXDigitalOptionSecurity() {
    final NonDeliverableFXDigitalOptionSecurity security = ExposureFunctionTestHelper.getNonDeliverableFXDigitalOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testNonDeliverableFXForwardSecurity() {
    final NonDeliverableFXForwardSecurity security = ExposureFunctionTestHelper.getNonDeliverableFXForwardSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testNonDeliverableFXOptionSecurity() {
    final NonDeliverableFXOptionSecurity security = ExposureFunctionTestHelper.getNonDeliverableFXOptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testPeriodicZeroDepositSecurity() {
    final PeriodicZeroDepositSecurity security = ExposureFunctionTestHelper.getPeriodicZeroDepositSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testSimpleZeroDepositSecurity() {
    final SimpleZeroDepositSecurity security = ExposureFunctionTestHelper.getSimpleZeroDepositSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testStandardFixedRecoveryCDSSecurity() {
    final StandardFixedRecoveryCDSSecurity security = ExposureFunctionTestHelper.getStandardFixedRecoveryCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testStandardRecoveryLockCDSSecurity() {
    final StandardRecoveryLockCDSSecurity security = ExposureFunctionTestHelper.getStandardRecoveryLockCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testStandardVanillaCDSSecurity() {
    final StandardVanillaCDSSecurity security = ExposureFunctionTestHelper.getStandardVanillaCDSSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testStockFutureSecurity() {
    final StockFutureSecurity future = ExposureFunctionTestHelper.getStockFutureSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(future);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals(1, ids.size());
    assertEquals(ExternalId.of(ContractCategoryExposureFunction.CONTRACT_IDENTIFIER, "Equity"), ids.get(0));
  }

  @Test
  public void testSwaptionSecurity() {
    final SwaptionSecurity security = ExposureFunctionTestHelper.getPaySwaptionSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testXCcySwapSecurity() {
    final SwapSecurity security = ExposureFunctionTestHelper.getXCcySwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testPayYoYInflationSwapSecurity() {
    final YearOnYearInflationSwapSecurity security = ExposureFunctionTestHelper.getPayYoYInflationSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testReceiveYoYInflationSwapSecurity() {
    final YearOnYearInflationSwapSecurity security = ExposureFunctionTestHelper.getPayYoYInflationSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testPayZeroCouponInflationSwapSecurity() {
    final ZeroCouponInflationSwapSecurity security = ExposureFunctionTestHelper.getPayZeroCouponInflationSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }

  @Test
  public void testReceiveZeroCouponInflationSwapSecurity() {
    final ZeroCouponInflationSwapSecurity security = ExposureFunctionTestHelper.getReceiveZeroCouponInflationSwapSecurity();
    final Trade trade = ExposureFunctionTestHelper.getTrade(security);
    final List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertNull(ids);
  }
}
