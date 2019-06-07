/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

/**
 * Publishes a {@link ManageableFunctionBlacklist} to remote clients
 */
public class DataManageableFunctionBlacklistFields {

  /**
   * Name of a field containing a rule in a request.
   */
  public static final String RULE_FIELD = "rule";
  /**
   * Name of a field containing the ttl in a request.
   */
  public static final String TTL_FIELD = "ttl";

}
