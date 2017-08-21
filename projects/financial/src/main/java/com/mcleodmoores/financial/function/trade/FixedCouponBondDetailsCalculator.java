/**
 *
 */
package com.mcleodmoores.financial.function.trade;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class FixedCouponBondDetailsCalculator extends
  InstrumentDerivativeVisitorAdapter<FixedCouponBondDetailsProvider, Object> {
  public static final FixedCouponBondDetailsCalculator INSTANCE = new FixedCouponBondDetailsCalculator();

  @Override
  public FixedCouponBondCashFlows visitBondFixedTransaction(final BondFixedTransaction bond, final FixedCouponBondDetailsProvider data) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(data, "data");
    final BondFixedSecurity underlying = bond.getBondTransaction();
    final AnnuityCouponFixedDefinition couponDefinitions = data.getDefinition().getUnderlyingBond().getCoupons();
    final Annuity<CouponFixed> coupons = underlying.getCoupon();
    final int n = coupons.getNumberOfPayments();
    final List<LocalDate> startAccrualDates = new ArrayList<>();
    final List<LocalDate> endAccrualDates = new ArrayList<>();
    final List<LocalDate> nominalPaymentDates = new ArrayList<>();
    final List<Double> discountFactors = new ArrayList<>();
    final List<Double> paymentTimes = new ArrayList<>();
    final List<Double> accrualFractions = new ArrayList<>();
    final List<CurrencyAmount> paymentAmounts = new ArrayList<>();
    final List<CurrencyAmount> notionals = new ArrayList<>();
    final List<Double> couponRates = new ArrayList<>();
    final double quantity = bond.getQuantity();
    for (int i = 0; i < n; i++) {
      final CouponFixedDefinition couponDefinition = couponDefinitions.getNthPayment(i);
      final CouponFixed coupon = coupons.getNthPayment(i);
      startAccrualDates.add(couponDefinition.getAccrualStartDate().toLocalDate());
      endAccrualDates.add(couponDefinition.getAccrualEndDate().toLocalDate());
      nominalPaymentDates.add(null);
      final double paymentTime = coupon.getPaymentTime();
      discountFactors.add(data.getCurves().getDiscountFactor(underlying.getIssuerEntity(), paymentTime));
      paymentTimes.add(paymentTime);
      accrualFractions.add(coupon.getPaymentYearFraction());
      final Currency currency = coupon.getCurrency();
      paymentAmounts.add(CurrencyAmount.of(currency, coupon.getAmount() * quantity));
      notionals.add(CurrencyAmount.of(currency, quantity));
      couponRates.add(couponDefinition.getRate());
    }
    final Annuity<PaymentFixed> nominals = underlying.getNominal();
    for (int i = 0; i < nominals.getNumberOfPayments(); i++) {
      final PaymentFixed payment = nominals.getNthPayment(i);
      final Currency currency = payment.getCurrency();
      startAccrualDates.add(null);
      endAccrualDates.add(null);
      nominalPaymentDates.add(data.getDefinition().getUnderlyingBond().getNominal().getNthPayment(i).getPaymentDate().toLocalDate());
      final double paymentTime = payment.getPaymentTime();
      discountFactors.add(data.getCurves().getDiscountFactor(underlying.getIssuerEntity(), paymentTime));
      paymentTimes.add(paymentTime);
      accrualFractions.add(null);
      paymentAmounts.add(CurrencyAmount.of(currency, payment.getAmount() * quantity));
      notionals.add(null);
      couponRates.add(null);
    }
    return new FixedCouponBondCashFlows(startAccrualDates, endAccrualDates, discountFactors, paymentTimes, accrualFractions,
        paymentAmounts, notionals, couponRates, nominalPaymentDates);
  }

}
