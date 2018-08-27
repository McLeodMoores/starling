/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.AnnuityAccrualDatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityFixedRatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityFixingDatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityFixingYearFractionsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityGearingsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityIndexTenorsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityNotionalsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentAmountsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentDatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentFractionsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentTimesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuitySpreadsVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.AnnuityDiscountFactorsVisitor;
import com.opengamma.analytics.financial.interestrate.swap.provider.AnnuityForwardRatesVisitor;
import com.opengamma.analytics.financial.interestrate.swap.provider.AnnuityProjectedPaymentsVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Calculator to expose the cash flows of the swap legs.
 */
public final class CashFlowDetailsCalculator extends InstrumentDerivativeVisitorAdapter<CashFlowDetailsProvider, SwapLegCashFlows> {

  @Override
  public SwapLegCashFlows visitFixedCouponSwap(final SwapFixedCoupon<?> derivative, final CashFlowDetailsProvider provider) {
    return getCashFlows(derivative, provider);
  }

  @Override
  public SwapLegCashFlows visitSwap(final Swap<?, ?> derivative, final CashFlowDetailsProvider provider) {
    return getCashFlows(derivative, provider);
  }

  private SwapLegCashFlows getCashFlows(final Swap<?, ?> swapDerivative, final CashFlowDetailsProvider provider) {
    AnnuityDefinition<? extends PaymentDefinition> legDefinition;
    final SwapDefinition swapDefinition = provider.getDefinition();
    Annuity<? extends Payment> legDerivative;
    final ZonedDateTime valuationTime = provider.getZonedDateTime();
    final MulticurveProviderInterface bundle = provider.getMulticurveProviderInterface();

    final boolean isDerivativeFirstLegPay = swapDerivative.getFirstLeg().getNthPayment(0).getReferenceAmount() < 0;
    final boolean isDefinitionFirstLegPay = swapDefinition.getFirstLeg().getNthPayment(0).getReferenceAmount() < 0;

    if (provider.getType() == PayReceiveType.PAY) {
      legDefinition = isDefinitionFirstLegPay ? swapDefinition.getFirstLeg() : swapDefinition.getSecondLeg();
      legDerivative = isDerivativeFirstLegPay ? swapDerivative.getFirstLeg() : swapDerivative.getSecondLeg();
    } else {
      legDefinition = isDefinitionFirstLegPay ? swapDefinition.getSecondLeg() : swapDefinition.getFirstLeg();
      legDerivative = isDerivativeFirstLegPay ? swapDerivative.getSecondLeg() : swapDerivative.getFirstLeg();
    }

    //sanitize the derivative/definition arrays
    final List<Double> paymentTimes = Doubles.asList(legDerivative.accept(AnnuityPaymentTimesVisitor.getInstance()));
    final List<Double> paymentFractions = Doubles.asList(legDerivative.accept(AnnuityPaymentFractionsVisitor.getInstance()));
    final List<Double> discountFactors = Doubles.asList(legDerivative.accept(AnnuityDiscountFactorsVisitor.getInstance(), bundle));
    final List<CurrencyAmount> notionals = Lists.newArrayList(legDefinition.accept(AnnuityNotionalsVisitor.getInstance(), valuationTime));
    final List<Double> fixedRates = Lists.newArrayList(legDerivative.accept(AnnuityFixedRatesVisitor.getInstance()));
    final List<CurrencyAmount> paymentAmounts = Lists.newArrayList(legDerivative.accept(AnnuityPaymentAmountsVisitor.getInstance()));

    final Pair<LocalDate[], LocalDate[]> accrualDates = legDefinition.accept(AnnuityAccrualDatesVisitor.getInstance(), valuationTime);
    final List<LocalDate> accrualStartDates = Lists.newArrayList(accrualDates.getFirst());
    final List<LocalDate> accrualEndDates = Lists.newArrayList(accrualDates.getSecond());

    if (provider.isFixed()) {
      return new FixedLegCashFlows(accrualStartDates,
          accrualEndDates,
          discountFactors,
          paymentTimes,
          paymentFractions,
          paymentAmounts,
          notionals,
          fixedRates);
    }

    //sanitize the derivative/definition arrays
    final List<Double> fixingYearFractions = Lists.newArrayList(legDefinition.accept(AnnuityFixingYearFractionsVisitor.getInstance(), valuationTime));
    final List<Double> forwardRates = Lists.newArrayList(legDerivative.accept(AnnuityForwardRatesVisitor.getInstance(), bundle));
    final List<Double> spreads = Doubles.asList(legDefinition.accept(AnnuitySpreadsVisitor.getInstance(), valuationTime));
    final List<Double> gearings = Doubles.asList(legDefinition.accept(AnnuityGearingsVisitor.getInstance(), valuationTime));
    final List<LocalDate> paymentDates = Lists.newArrayList(legDefinition.accept(AnnuityPaymentDatesVisitor.getInstance(), valuationTime));
    final List<CurrencyAmount> projectedAmounts = Lists.newArrayList(legDerivative.accept(AnnuityProjectedPaymentsVisitor.getInstance(), bundle));
    final List<Tenor> indexTenors = Lists.newArrayList(legDefinition.accept(AnnuityIndexTenorsVisitor.getInstance(), valuationTime));

    final Pair<LocalDate[], LocalDate[]> fixingDates = legDefinition.accept(AnnuityFixingDatesVisitor.getInstance(), valuationTime);
    final List<LocalDate> fixingStartDates = Lists.newArrayList(fixingDates.getFirst());
    final List<LocalDate> fixingEndDates = Lists.newArrayList(fixingDates.getSecond());

    return new FloatingLegCashFlows(accrualStartDates,
        accrualEndDates,
        paymentFractions,
        fixingStartDates,
        fixingEndDates,
        fixingYearFractions,
        forwardRates,
        fixedRates,
        paymentDates,
        paymentTimes,
        discountFactors,
        paymentAmounts,
        projectedAmounts,
        notionals,
        spreads,
        gearings,
        indexTenors);
  }

}
