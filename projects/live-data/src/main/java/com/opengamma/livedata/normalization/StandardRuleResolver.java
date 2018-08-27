/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Collection;

import com.opengamma.livedata.resolver.NormalizationRuleResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@code NormalizationRuleResolver} that gets the normalization rule set
 * from a fixed collection.
 */
public class StandardRuleResolver implements NormalizationRuleResolver {

  private final Collection<NormalizationRuleSet> _rules;

  public StandardRuleResolver(final Collection<NormalizationRuleSet> rules) {
    ArgumentChecker.notNull(rules, "Supported rules");
    _rules = rules;
  }

  @Override
  public NormalizationRuleSet resolve(final String ruleSetId) {
    ArgumentChecker.notNull(ruleSetId, "Rule set ID");

    for (final NormalizationRuleSet normalizationRuleSet : _rules) {
      if (ruleSetId.equals(normalizationRuleSet.getId())) {
        return normalizationRuleSet;
      }
    }

    return null;
  }

}
