/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.forward;

/**
 * A Hibernate bean representation of
 * {@link com.opengamma.financial.security.forward.AgricultureForwardSecurity}.
 */
public class AgricultureForwardSecurityBean extends CommodityForwardSecurityBean {

  @Override
  public <T> T accept(final Visitor<T> visitor) {
    return visitor.visitAgricultureForwardType(this);
  }

}
