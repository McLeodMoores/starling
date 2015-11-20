/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.jms.JmsConnector;

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
