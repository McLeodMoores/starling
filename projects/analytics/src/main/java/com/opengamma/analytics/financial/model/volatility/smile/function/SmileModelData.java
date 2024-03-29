/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

/**
 *
 */
public interface SmileModelData {

  int getNumberOfParameters();

  double getParameter(int index);

  boolean isAllowed(int index, double value);

  SmileModelData with(int index, double value);

}
