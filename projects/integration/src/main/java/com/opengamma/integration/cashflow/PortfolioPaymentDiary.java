/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.cashflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.threeten.bp.LocalDate;

/**
 *
 */
public class PortfolioPaymentDiary {

  private final SortedMap<LocalDate, List<PositionPayment>> _paymentDiary = new TreeMap<>();

  public void add(final LocalDate date, final PositionPayment payment) {
    List<PositionPayment> payments = _paymentDiary.get(date);
    if (payments == null) {
      payments = new ArrayList<>();
      _paymentDiary.put(date, payments);
    }
    payments.add(payment);
  }

  public List<LocalDate> getPaymentDates() {
    return new ArrayList<>(_paymentDiary.keySet());
  }

  public List<PositionPayment> getPayments(final LocalDate date) {
    return Collections.unmodifiableList(_paymentDiary.get(date));
  }

}
