/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;

/**
 * Changes the name of a field, leaving its value and all other fields unaffected.
 */
public class FieldNameChange implements NormalizationRule {

  private final String _from;
  private final String _to;

  public FieldNameChange(final String from, final String to) {
    ArgumentChecker.notNull(from, "From");
    ArgumentChecker.notNull(to, "To");
    _from = from;
    _to = to;
  }

  @Override
  public MutableFudgeMsg apply(final MutableFudgeMsg msg, final String securityUniqueId, final FieldHistoryStore fieldHistory) {
    final FudgeField field = msg.getByName(_from);
    if (field != null) {
      final Object value = field.getValue();
      msg.remove(_from);
      msg.add(_to, null, field.getType(), value);
    }
    return msg;
  }

}
