/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.json;

import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert an {@link IborIndexConvention} to JSON and back again.
 */
public final class IborIndexConventionBuilder extends AbstractJSONBuilder<IborIndexConvention> {
  /**
   * Static instance.
   */
  public static final IborIndexConventionBuilder INSTANCE = new IborIndexConventionBuilder();

  @Override
  public IborIndexConvention fromJSON(final String json) {
    return fromJSON(IborIndexConvention.class, ArgumentChecker.notNull(json, "json"));
  }

  @Override
  public String toJSON(final IborIndexConvention object) {
    return fudgeToJson(ArgumentChecker.notNull(object, "object"));
  }

  @Override
  public String getTemplate() {
    return null;
  }

  private IborIndexConventionBuilder() {
  }
}
