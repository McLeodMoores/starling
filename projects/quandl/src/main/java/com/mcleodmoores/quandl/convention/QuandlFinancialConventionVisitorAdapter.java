/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.convention;

import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.FinancialConventionVisitorAdapter;
import com.opengamma.util.ArgumentChecker;

/**
 * An adapter for {@link QuandlFinancialConventionVisitor} that extends {@link FinancialConventionVisitorAdapter}
 * and provides default implementations that throw {@link Quandl4OpenGammaRuntimeException} for Quandl financial conventions.
 *
 * @param <T> The type of the result.
 */
public class QuandlFinancialConventionVisitorAdapter<T> extends FinancialConventionVisitorAdapter<T> implements QuandlFinancialConventionVisitor<T> {

  @Override
  public T visitQuandlStirFutureConvention(final QuandlStirFutureConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitQuandlFedFundsFutureConvention(final QuandlFedFundsFutureConvention convention) {
    return getErrorMessage(convention);
  }

  /**
   * Creates an error message for a convention.
   * @param convention  the convention
   * @return  an error message
   */
  private T getErrorMessage(final FinancialConvention convention) {
    ArgumentChecker.notNull(convention, "convention");
    throw new Quandl4OpenGammaRuntimeException(this.getClass().getName() + " does not support conventions of type " + convention.getClass());
  }
}
