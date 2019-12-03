/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.annuity;

import java.util.Comparator;

import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;

/**
 * A comparator for all payment types that compares the final payment / settlement time.
 */
public final class PaymentComparator implements Comparator<Payment> {

  /**
   * A static instance.
   */
  public static final Comparator<Payment> INSTANCE = new PaymentComparator();

  @Override
  public int compare(final Payment payment1, final Payment payment2) {
    final double delta = payment1.accept(LastTimeCalculator.getInstance()) - payment2.accept(LastTimeCalculator.getInstance());
    if (delta > 0) {
      return 1;
    }
    if (delta < 0) {
      return -1;
    }
    return 0;
  }

  private PaymentComparator() {
  }
}
