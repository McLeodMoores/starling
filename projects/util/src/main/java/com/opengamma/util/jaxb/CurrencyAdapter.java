/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.opengamma.util.money.Currency;

/**
 * XML adapter for Currency.
 */
public class CurrencyAdapter extends XmlAdapter<String, Currency> {

  @Override
  public Currency unmarshal(final String v) throws Exception {
    return Currency.of(v);
  }

  @Override
  public String marshal(final Currency v) throws Exception {
    return v.getCode();
  }

}
