/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

/**
 * A Hibernate bean representation of
 * {@link com.opengamma.financial.security.future.EnergyFutureSecurity}.
 */
public class EnergyFutureBean extends CommodityFutureBean {

  @Override
  public <T> T accept(final Visitor<T> visitor) {
    return visitor.visitEnergyFutureType(this);
  }

}
