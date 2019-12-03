/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing;

/**
 *
 */
public class OptionPricingException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public OptionPricingException() {
    super();
  }

  public OptionPricingException(final String s) {
    super(s);
  }

  public OptionPricingException(final String s, final Throwable cause) {
    super(s, cause);
  }

  public OptionPricingException(final Throwable cause) {
    super(cause);
  }
}
