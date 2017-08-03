/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.trade;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class FxForwardDetails {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private CurrencyAmount _payAmount;
    private CurrencyAmount _receiveAmount;
    private double _payDiscountFactor;
    private double _payZeroRate;
    private double _receiveDiscountFactor;
    private double _receiveZeroRate;
    private double _paymentTime;

    public Builder withPayAmount(final CurrencyAmount payAmount) {
      _payAmount = ArgumentChecker.notNull(payAmount, "payAmount");
      return this;
    }

    public Builder withReceiveAmount(final CurrencyAmount receiveAmount) {
      _receiveAmount = ArgumentChecker.notNull(receiveAmount, "receiveAmount");
      return this;
    }

    public Builder withPayDiscountFactor(final double payDiscountFactor) {
      _payDiscountFactor = payDiscountFactor;
      return this;
    }

    public Builder withReceiveDiscountFactor(final double receiveDiscountFactor) {
      _receiveDiscountFactor = receiveDiscountFactor;
      return this;
    }

    public Builder withPayZeroRate(final double payZeroRate) {
      _payZeroRate = payZeroRate;
      return this;
    }

    public Builder withReceiveZeroRate(final double receiveZeroRate) {
      _receiveZeroRate = receiveZeroRate;
      return this;
    }

    public Builder withPaymentTime(final double paymentTime) {
      _paymentTime = paymentTime;
      return this;
    }

    public FxForwardDetails build() {
      return new FxForwardDetails(_payAmount, _receiveAmount, _payDiscountFactor, _receiveDiscountFactor, _payZeroRate, _receiveZeroRate, _paymentTime);
    }
  }

  private final CurrencyAmount _payAmount;
  private final CurrencyAmount _receiveAmount;
  private final double _payDiscountFactor;
  private final double _payZeroRate;
  private final double _receiveDiscountFactor;
  private final double _receiveZeroRate;
  private final double _paymentTime;

  FxForwardDetails(final CurrencyAmount payAmount, final CurrencyAmount receiveAmount, final double payDiscountFactor,
      final double receiveDiscountFactor, final double payZeroRate, final double receiveZeroRate, final double paymentTime) {
    _payAmount = payAmount;
    _receiveAmount = receiveAmount;
    _payDiscountFactor = payDiscountFactor;
    _receiveDiscountFactor = receiveDiscountFactor;
    _payZeroRate = payZeroRate;
    _receiveZeroRate = receiveZeroRate;
    _paymentTime = paymentTime;
  }

  public CurrencyAmount getPayAmount() {
    return _payAmount;
  }

  public CurrencyAmount getReceiveAmount() {
    return _receiveAmount;
  }

  public double getPayDiscountFactor() {
    return _payDiscountFactor;
  }

  public double getPayZeroRate() {
    return _payZeroRate;
  }

  public double getReceiveDiscountFactor() {
    return _receiveDiscountFactor;
  }

  public double getReceiveZeroRate() {
    return _receiveZeroRate;
  }

  public double getPaymentTime() {
    return _paymentTime;
  }


}
