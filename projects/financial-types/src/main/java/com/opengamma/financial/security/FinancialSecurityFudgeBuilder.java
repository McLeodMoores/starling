/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.master.security.ManageableSecurityFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code FinancialSecurity}.
 */
public class FinancialSecurityFudgeBuilder extends AbstractFudgeBuilder {

  /**
   * Converts a security to a Fudge message.
   * 
   * @param serializer
   *          the Fudge serializer, not null
   * @param object
   *          the security, not null
   * @param msg
   *          the message to add security information to, not null
   */
  public static void toFudgeMsg(final FudgeSerializer serializer, final FinancialSecurity object, final MutableFudgeMsg msg) {
    ManageableSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
  }

  /**
   * Converts a Fudge message to a security.
   * 
   * @param deserializer
   *          the Fudge deserializer, not null
   * @param msg
   *          the message, not null
   * @param object
   *          the security to have its fields populated, not null
   */
  public static void fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg, final FinancialSecurity object) {
    ManageableSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
  }

}
