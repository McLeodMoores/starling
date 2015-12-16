/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 * Constants for Quandl data.
 */
public final class QuandlConstants {

  /**
   * Data source name.
   */
  public static final String QUANDL_DATA_SOURCE_NAME = "QUANDL";

  /**
   * Quandl prefix name.
   */
  public static final String QUANDL_DATA_PREFIX = "QUANDL_PREFIX";

  /**
   * Quandl rate name.
   */
  public static final String QUANDL_RATE = "QUANDL_RATE";

  /**
   * Quandl rate scheme name.
   */
  public static final ExternalScheme QUANDL_RATE_SCHEME = ExternalScheme.of(QUANDL_RATE);

  /**
   * The Quandl code scheme.
   */
  public static final ExternalScheme QUANDL_CODE = ExternalScheme.of(QUANDL_DATA_SOURCE_NAME);

  /**
   * The Quandl prefix scheme.
   */
  public static final ExternalScheme QUANDL_PREFIX = ExternalScheme.of(QUANDL_DATA_PREFIX);

  /**
   * The Value field name.
   */
  public static final String VALUE_FIELD_NAME = "Value";

  /**
   * The Rate field name.
   */
  public static final String RATE_FIELD_NAME = "Rate";

  /**
   * The Last field name.
   */
  public static final String LAST_FIELD_NAME = "Last";

  /**
   * The High field name.
   */
  public static final String HIGH_FIELD_NAME = "High";

  /**
   * The Low field name.
   */
  public static final String LOW_FIELD_NAME = "Low";

  /**
   * The Open field name.
   */
  public static final String OPEN_FIELD_NAME = "Open";

  /**
   * The Settle field name.
   */
  public static final String SETTLE_FIELD_NAME = "Settle";

  /**
   * The default provider.
   */
  public static final String DEFAULT_PROVIDER = "DEFAULT";

  /**
   * Restricted constructor.
   */
  private QuandlConstants() {
  }

  /**
   * Creates an external id for Quandl codes.
   * @param code The code, not null
   * @return The id
   */
  public static ExternalId ofCode(final String code) {
    return ExternalId.of(QUANDL_CODE, code);
  }

  /**
   * Creates an external id for Quandl prefixes.
   * @param code The prefix, not null
   * @return The id
   */
  public static ExternalId ofPrefix(final String code) {
    return ExternalId.of(QUANDL_DATA_PREFIX, code);
  }
}
