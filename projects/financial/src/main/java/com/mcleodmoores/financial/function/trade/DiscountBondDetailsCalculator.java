/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.trade;

import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class DiscountBondDetailsCalculator extends
    InstrumentDerivativeVisitorAdapter<DiscountBondDetailsProvider, DiscountBondCashFlows> {
  public static final DiscountBondDetailsCalculator INSTANCE = new DiscountBondDetailsCalculator();

  @Override
  public DiscountBondCashFlows visitBillTransaction(final BillTransaction bill, final DiscountBondDetailsProvider data) {
    ArgumentChecker.notNull(bill, "bill");
    ArgumentChecker.notNull(data, "data");
    final BillSecurityDefinition definition = data.getDefinition().getUnderlying();
    final BillSecurity underlying = bill.getBillPurchased();
    final double paymentTime = underlying.getEndTime();
    final Currency currency = definition.getCurrency();
    return new DiscountBondCashFlows(definition.getEndDate().toLocalDate(),
        CurrencyAmount.of(currency, underlying.getNotional() * bill.getQuantity()),
        paymentTime,
        data.getCurves().getDiscountFactor(underlying.getIssuerEntity(), paymentTime));
  }
}
