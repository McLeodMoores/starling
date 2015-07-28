/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;


/**
 * Shared field names for {@link FunctionBlacklist} remote clients
 */
public class DataFunctionBlacklistFields {

  /**
   * Field containing the blacklist name when included in a response message.
   */
  public static final String NAME_FIELD = "name";
  /**
   * Field containing the blacklist modification count when included in a response message.
   */
  public static final String MODIFICATION_COUNT_FIELD = "modificationCount";
  /**
   * Field containing the blacklist rules when included in a response message.
   */
  public static final String RULES_FIELD = "rules";
  /**
   * Field containing new blacklist rules when published to remote listeners.
   */
  public static final String RULES_ADDED_FIELD = "add";
  /**
   * Field containing removed blacklist rules when published to remote listeners.
   */
  public static final String RULES_REMOVED_FIELD = "remove";
  /**
   * Field containing the JMS topic name updates will be published on.
   */
  public static final String JMS_TOPIC_FIELD = "jms";

  

}
