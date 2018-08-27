/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;

/**
 * Multiplies the value of a {@code Double} field by a security-dependent value.
 */
public class SecurityRuleApplier implements NormalizationRule {
  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityRuleApplier.class);

  private final SecurityRuleProvider _ruleProvider;

  public SecurityRuleApplier(final SecurityRuleProvider ruleProvider) {
    ArgumentChecker.notNull(ruleProvider, "ruleProvider");
    _ruleProvider = ruleProvider;
  }

  @Override
  public MutableFudgeMsg apply(final MutableFudgeMsg msg, final String securityUniqueId, final FieldHistoryStore fieldHistory) {
    NormalizationRule rule;
    try {
      rule = _ruleProvider.getRule(securityUniqueId);
      if (rule == null) {
        return msg;
      }
    } catch (final Exception e) {
      LOGGER.warn("Failed to get normalization rule for security id {} : {}", securityUniqueId, e.getMessage());
      return null;
    }

    try {
      return rule.apply(msg, securityUniqueId, fieldHistory);
    } catch (final Exception e) {
      LOGGER.debug("Rule {} rejected message with exception {}", rule.toString(), e.getMessage());
      // Interpret an exception as a rejection of the message
      return null;
    }
  }

}
