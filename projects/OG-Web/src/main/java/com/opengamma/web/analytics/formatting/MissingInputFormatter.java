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

import com.opengamma.engine.cache.MissingInput;
import com.opengamma.engine.value.ValueSpecification;

/*package*/ class MissingInputFormatter extends AbstractFormatter<MissingInput> {

  /*package*/ MissingInputFormatter() {
    super(MissingInput.class);
    addFormatter(new Formatter<MissingInput>(Format.HISTORY) {
      @Override
      protected Object formatValue(final MissingInput value, final ValueSpecification valueSpec, final Object inlineKey) {
        return null;
      }
    });
  }

  @Override
  public Object formatCell(final MissingInput value, final ValueSpecification valueSpec, final Object inlineKey) {
    return value.toString();
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }

}
