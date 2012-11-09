/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import com.opengamma.analytics.financial.commodity.definition.AgricultureForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureOptionDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.DeliverableSwapFuturesSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponOISSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborSpreadDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * A convenience class that means that classes implementing InstrumentDefinitionVisitor do not have to implement every method.
 * @param <DATA_TYPE> Type of the data
 * @param <RESULT_TYPE> Type of the result
 */
public class InstrumentDefinitionVisitorAdapter<DATA_TYPE, RESULT_TYPE> implements InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> {
  private final RESULT_TYPE _defaultReturnType;

  public InstrumentDefinitionVisitorAdapter() {
    _defaultReturnType = null;
  }

  public InstrumentDefinitionVisitorAdapter(final RESULT_TYPE defaultReturnType) {
    ArgumentChecker.notNull(defaultReturnType, "default return type");
    _defaultReturnType = defaultReturnType;
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond, final DATA_TYPE data) {
    return getDefaultValue(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond) {
    return getDefaultValue(bond);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond, final DATA_TYPE data) {
    return getDefaultValue(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond) {
    return getDefaultValue(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureSecurityDefinition(final BondFutureDefinition bond, final DATA_TYPE data) {
    return getDefaultValue(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureSecurityDefinition(final BondFutureDefinition bond) {
    return getDefaultValue(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond, final DATA_TYPE data) {
    return getDefaultValue(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond) {
    return getDefaultValue(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond, final DATA_TYPE data) {
    return getDefaultValue(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond) {
    return getDefaultValue(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond, final DATA_TYPE data) {
    return getDefaultValue(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond) {
    return getDefaultValue(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond, final DATA_TYPE data) {
    return getDefaultValue(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond) {
    return getDefaultValue(bond);
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(final BillSecurityDefinition bill, final DATA_TYPE data) {
    return getDefaultValue(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(final BillSecurityDefinition bill) {
    return getDefaultValue(bill);
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(final BillTransactionDefinition bill, final DATA_TYPE data) {
    return getDefaultValue(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(final BillTransactionDefinition bill) {
    return getDefaultValue(bill);
  }

  @Override
  public RESULT_TYPE visitCashDefinition(final CashDefinition cash, final DATA_TYPE data) {
    return getDefaultValue(cash, data);
  }

  @Override
  public RESULT_TYPE visitCashDefinition(final CashDefinition cash) {
    return getDefaultValue(cash);
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(final DepositIborDefinition deposit, final DATA_TYPE data) {
    return getDefaultValue(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(final DepositIborDefinition deposit) {
    return getDefaultValue(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit, final DATA_TYPE data) {
    return getDefaultValue(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit) {
    return getDefaultValue(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(final DepositZeroDefinition deposit, final DATA_TYPE data) {
    return getDefaultValue(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(final DepositZeroDefinition deposit) {
    return getDefaultValue(deposit);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra, final DATA_TYPE data) {
    return getDefaultValue(fra, data);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
    return getDefaultValue(fra);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(final InterestRateFutureDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(final InterestRateFutureDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(final PaymentFixedDefinition payment, final DATA_TYPE data) {
    return getDefaultValue(payment, data);
  }

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(final PaymentFixedDefinition payment) {
    return getDefaultValue(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(final CouponFixedDefinition payment, final DATA_TYPE data) {
    return getDefaultValue(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(final CouponFixedDefinition payment) {
    return getDefaultValue(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(final CouponIborDefinition payment, final DATA_TYPE data) {
    return getDefaultValue(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(final CouponIborDefinition payment) {
    return getDefaultValue(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment, final DATA_TYPE data) {
    return getDefaultValue(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment) {
    return getDefaultValue(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment, final DATA_TYPE data) {
    return getDefaultValue(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment) {
    return getDefaultValue(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundedDefinition(final CouponIborCompoundedDefinition payment, final DATA_TYPE data) {
    return getDefaultValue(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundedDefinition(final CouponIborCompoundedDefinition payment) {
    return getDefaultValue(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment, final DATA_TYPE data) {
    return getDefaultValue(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment) {
    return getDefaultValue(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorIborDefinition(final CapFloorIborDefinition capFloor, final DATA_TYPE data) {
    return getDefaultValue(capFloor, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorIborDefinition(final CapFloorIborDefinition capFloor) {
    return getDefaultValue(capFloor);
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplifiedDefinition(final CouponOISSimplifiedDefinition payment, final DATA_TYPE data) {
    return getDefaultValue(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplifiedDefinition(final CouponOISSimplifiedDefinition payment) {
    return getDefaultValue(payment);
  }

  @Override
  public RESULT_TYPE visitCouponOISDefinition(final CouponOISDefinition payment, final DATA_TYPE data) {
    return getDefaultValue(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOISDefinition(final CouponOISDefinition payment) {
    return getDefaultValue(payment);
  }

  @Override
  public RESULT_TYPE visitCouponCMSDefinition(final CouponCMSDefinition payment, final DATA_TYPE data) {
    return getDefaultValue(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponCMSDefinition(final CouponCMSDefinition payment) {
    return getDefaultValue(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSDefinition(final CapFloorCMSDefinition capFloor, final DATA_TYPE data) {
    return getDefaultValue(capFloor, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSDefinition(final CapFloorCMSDefinition capFloor) {
    return getDefaultValue(capFloor);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition capFloor, final DATA_TYPE data) {
    return getDefaultValue(capFloor, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition capFloorCMSSpread) {
    return getDefaultValue(capFloorCMSSpread);
  }

  @Override
  public RESULT_TYPE visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final DATA_TYPE data) {
    return getDefaultValue(annuity, data);
  }

  @Override
  public RESULT_TYPE visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    return getDefaultValue(annuity);
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(final SwapDefinition swap, final DATA_TYPE data) {
    return getDefaultValue(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(final SwapDefinition swap) {
    return getDefaultValue(swap);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final DATA_TYPE data) {
    return getDefaultValue(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
    return getDefaultValue(swap);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final DATA_TYPE data) {
    return getDefaultValue(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
    return getDefaultValue(swap);
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final DATA_TYPE data) {
    return getDefaultValue(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
    return getDefaultValue(swap);
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap, final DATA_TYPE data) {
    return getDefaultValue(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap) {
    return getDefaultValue(swap);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption, final DATA_TYPE data) {
    return getDefaultValue(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption) {
    return getDefaultValue(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption, final DATA_TYPE data) {
    return getDefaultValue(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption) {
    return getDefaultValue(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption, final DATA_TYPE data) {
    return getDefaultValue(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption) {
    return getDefaultValue(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption, final DATA_TYPE data) {
    return getDefaultValue(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption) {
    return getDefaultValue(swaption);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon, final DATA_TYPE data) {
    return getDefaultValue(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon) {
    return getDefaultValue(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon, final DATA_TYPE data) {
    return getDefaultValue(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon) {
    return getDefaultValue(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon, final DATA_TYPE data) {
    return getDefaultValue(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon) {
    return getDefaultValue(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon, final DATA_TYPE data) {
    return getDefaultValue(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon) {
    return getDefaultValue(coupon);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond, final DATA_TYPE data) {
    return getDefaultValue(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond) {
    return getDefaultValue(bond);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond, final DATA_TYPE data) {
    return getDefaultValue(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond) {
    return getDefaultValue(bond);
  }

  @Override
  public RESULT_TYPE visitCDSDefinition(final ISDACDSDefinition cds, final DATA_TYPE data) {
    return getDefaultValue(cds, data);
  }

  @Override
  public RESULT_TYPE visitCDSDefinition(final ISDACDSDefinition cds) {
    return getDefaultValue(cds);
  }

  @Override
  public RESULT_TYPE visitForexDefinition(final ForexDefinition fx, final DATA_TYPE data) {
    return getDefaultValue(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexDefinition(final ForexDefinition fx) {
    return getDefaultValue(fx);
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(final ForexSwapDefinition fx, final DATA_TYPE data) {
    return getDefaultValue(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(final ForexSwapDefinition fx) {
    return getDefaultValue(fx);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx, final DATA_TYPE data) {
    return getDefaultValue(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx) {
    return getDefaultValue(fx);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx, final DATA_TYPE data) {
    return getDefaultValue(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx) {
    return getDefaultValue(fx);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf, final DATA_TYPE data) {
    return getDefaultValue(ndf, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf) {
    return getDefaultValue(ndf);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo, final DATA_TYPE data) {
    return getDefaultValue(ndo, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo) {
    return getDefaultValue(ndo);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx, final DATA_TYPE data) {
    return getDefaultValue(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx) {
    return getDefaultValue(fx);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(final DeliverableSwapFuturesSecurityDefinition futures, final DATA_TYPE data) {
    return getDefaultValue(futures, data);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(final DeliverableSwapFuturesSecurityDefinition futures) {
    return getDefaultValue(futures);
  }

  @Override
  public RESULT_TYPE visitMetalForwardDefinition(final MetalForwardDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitMetalForwardDefinition(final MetalForwardDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureDefinition(final MetalFutureDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureDefinition(final MetalFutureDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOptionDefinition(final MetalFutureOptionDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOptionDefinition(final MetalFutureOptionDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureForwardDefinition(final AgricultureForwardDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureForwardDefinition(final AgricultureForwardDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureDefinition(final AgricultureFutureDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureDefinition(final AgricultureFutureDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOptionDefinition(final AgricultureFutureOptionDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOptionDefinition(final AgricultureFutureOptionDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitEnergyForwardDefinition(final EnergyForwardDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitEnergyForwardDefinition(final EnergyForwardDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureDefinition(final EnergyFutureDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureDefinition(final EnergyFutureDefinition future) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOptionDefinition(final EnergyFutureOptionDefinition future, final DATA_TYPE data) {
    return getDefaultValue(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOptionDefinition(final EnergyFutureOptionDefinition future) {
    return getDefaultValue(future);
  }

  private RESULT_TYPE getDefaultValue(final InstrumentDefinition<?> definition, final DATA_TYPE data) {
    if (_defaultReturnType != null) {
      return _defaultReturnType;
    }
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support definitions of type " + definition.getClass().getSimpleName()
        + " with data of type " + data.getClass().getSimpleName());
  }

  private RESULT_TYPE getDefaultValue(final InstrumentDefinition<?> definition) {
    if (_defaultReturnType != null) {
      return _defaultReturnType;
    }
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support definitions of type " + definition.getClass().getSimpleName());
  }

}