/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.convention;

import com.opengamma.financial.convention.FinancialConventionVisitor;

/**
 * A visitor for project-specific conventions that extends {@link FinancialConventionVisitor}.
 *
 * @param <T> The type of the result
 */
public interface QuandlFinancialConventionVisitor<T> extends FinancialConventionVisitor<T> {

  /**
   * Visits {@link QuandlStirFutureConvention}.
   * @param convention The convention, not null
   * @return The result
   */
  T visitQuandlStirFutureConvention(QuandlStirFutureConvention convention);

  /**
   * Visits {@link QuandlFedFundsFutureConvention}.
   * @param convention The convention, not null
   * @return The result
   */
  T visitQuandlFedFundsFutureConvention(QuandlFedFundsFutureConvention convention);

}
