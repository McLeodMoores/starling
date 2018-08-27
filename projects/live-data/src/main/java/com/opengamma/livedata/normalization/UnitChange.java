/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Set;

import org.fudgemsg.MutableFudgeMsg;

import com.google.common.collect.ImmutableSet;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;

/**
 * Multiplies the value of a {@code Double} field by a constant.
 */
public class UnitChange implements NormalizationRule {

  private final Set<String> _fields;
  private final double _multiplier;

  public UnitChange(final String field, final double multiplier) {
    ArgumentChecker.notNull(field, "Field name");
    _fields = ImmutableSet.of(field);
    _multiplier = multiplier;
  }

  public UnitChange(final Set<String> fields, final double multiplier) {
    ArgumentChecker.notNull(fields, "Field names");
    _fields = fields;
    _multiplier = multiplier;
  }

  public UnitChange(final double multiplier, final String... fields) {
    ArgumentChecker.notNull(fields, "fields");
    _fields = ImmutableSet.copyOf(fields);
    _multiplier = multiplier;
  }

  @Override
  public MutableFudgeMsg apply(final MutableFudgeMsg msg, final String securityUniqueId, final FieldHistoryStore fieldHistory) {
    return multiplyFields(msg, _fields, _multiplier);
  }

  private static MutableFudgeMsg multiplyFields(final MutableFudgeMsg msg, final Set<String> fields, final double multiplier) {
    for (final String field : fields) {
      final Double value = msg.getDouble(field);
      if (value != null) {
        final double newValue = value * multiplier;
        msg.remove(field);
        msg.add(field, newValue);
      }
    }
    return msg;
  }

}
