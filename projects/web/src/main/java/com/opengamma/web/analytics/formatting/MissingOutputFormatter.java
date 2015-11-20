/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.cache.MissingOutput;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Formats instances of {@code MissingOutput} which are placeholders in the analytics results for values
 * that couldn't be calculated.
 */
/*package*/ class MissingOutputFormatter extends AbstractFormatter<MissingOutput> {

  /*package*/ MissingOutputFormatter() {
    super(MissingOutput.class);
    addFormatter(new Formatter<MissingOutput>(Format.HISTORY) {
      @Override
      protected Object formatValue(final MissingOutput value, final ValueSpecification valueSpec, final Object inlineKey) {
        return null;
      }
    });
    addFormatter(new Formatter<MissingOutput>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final MissingOutput value, final ValueSpecification valueSpec, final Object inlineKey) {
        return value.toString();
      }
    });
  }

  @Override
  public Object formatCell(final MissingOutput value, final ValueSpecification valueSpec, final Object inlineKey) {
    return value.toString();
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }

}
