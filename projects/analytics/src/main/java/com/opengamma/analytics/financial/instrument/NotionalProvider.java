/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.instrument;

import org.threeten.bp.LocalDate;

/**
 * An interface that can return the notional for a given date.
 */
public interface NotionalProvider {

  double getAmount(LocalDate date);

}
