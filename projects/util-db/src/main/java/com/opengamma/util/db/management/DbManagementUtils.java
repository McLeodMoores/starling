/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Utilities around {@link DbManagement} instances.
 */
public final class DbManagementUtils {

  private static final Map<String, DbManagement> JDBS_VENDOR_MAP = new ConcurrentHashMap<>();

  static {
    JDBS_VENDOR_MAP.put("postgresql", PostgresDbManagement.getInstance());
    JDBS_VENDOR_MAP.put("derby", DerbyDbManagement.getInstance());
    JDBS_VENDOR_MAP.put("hsqldb", HSQLDbManagement.getInstance());
    JDBS_VENDOR_MAP.put("sqlserver", SqlServer2008DbManagement.getInstance());
    JDBS_VENDOR_MAP.put("oracle", Oracle11gDbManagement.getInstance());
  }

  /**
   * Hidden constructor
   */
  private DbManagementUtils() {
  }

  /**
   * Gets the {@link DbManagement} implementation for a JDBC vendor name.
   *
   * @param jdbcUrl  the JDBC url, not null
   * @return the {@link DbManagement} implementation, not null
   * @throws IllegalArgumentException  if the given JDBC vendor name is unsupported
   */
  public static DbManagement getDbManagement(final String jdbcUrl) {
    ArgumentChecker.notNull(jdbcUrl, "jdbcUrl");
    final String[] dbUrlParts = jdbcUrl.toLowerCase().split(":");
    if (dbUrlParts.length < 2 || !"jdbc".equals(dbUrlParts[0])) {
      throw new OpenGammaRuntimeException("Expected JDBC database URL, found '" + jdbcUrl + "'");
    }
    final String jdbcVendorName = dbUrlParts[1];
    final DbManagement dbManagement = JDBS_VENDOR_MAP.get(jdbcVendorName);
    if (dbManagement == null) {
      throw new IllegalArgumentException("Unsupported JDBC vendor name '" + jdbcVendorName + "'");
    }
    return dbManagement;
  }

}
