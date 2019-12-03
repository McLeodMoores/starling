/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.lookup.SecurityValueProvider;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 *
 */
public class SwapPayReceiveProvider implements SecurityValueProvider<SwapSecurity> {

  @Override
  public String getValue(final SwapSecurity security) {
    final FixedFloatVisitor visitor = new FixedFloatVisitor();
    final Boolean payFixed = security.getPayLeg().accept(visitor);
    final Boolean receiveFixed = security.getReceiveLeg().accept(visitor);
    // for fixed/float swaps it's taken from the fixed leg, for float/float it doesn't make any sense
    if (payFixed) {
      return "Pay";
    } else if (receiveFixed) {
      return "Receive";
    } else {
      return null;
    }
  }
}
