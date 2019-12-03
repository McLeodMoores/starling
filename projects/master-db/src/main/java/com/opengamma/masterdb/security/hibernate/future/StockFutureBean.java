/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

/**
 * A Hibernate bean representation of
 * {@link com.opengamma.financial.security.future.StockFutureSecurity}.
 */
public class StockFutureBean extends FutureSecurityBean {

  @Override
  public <T> T accept(final Visitor<T> visitor) {
    return visitor.visitStockFutureType(this);
  }

}
