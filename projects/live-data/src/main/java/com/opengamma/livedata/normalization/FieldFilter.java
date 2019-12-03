/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Collection;
import java.util.HashSet;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeMsg;

import com.google.common.collect.Sets;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Strips all fields out of the message except the ones you want to explicitly accept.
 * <p>
 * If no field is accepted, the message is extinguished.
 */
public class FieldFilter implements NormalizationRule {

  private final Collection<String> _fieldsToAccept;
  private final FudgeContext _context;

  /**
   * @param fieldsToAccept
   *          the fields to accept
   */
  public FieldFilter(final String... fieldsToAccept) {
    this(OpenGammaFudgeContext.getInstance(), fieldsToAccept);
  }

  /**
   * @param context
   *          a Fudge context, not null
   * @param fieldsToAccept
   *          the fields to accept
   */
  public FieldFilter(final FudgeContext context, final String... fieldsToAccept) {
    this(Sets.newHashSet(fieldsToAccept), context);
  }

  /**
   * @param fieldsToAccept
   *          the fields to accept, not null
   */
  public FieldFilter(final Collection<String> fieldsToAccept) {
    this(fieldsToAccept, OpenGammaFudgeContext.getInstance());
  }

  /**
   * @param fieldsToAccept
   *          the fields to accept, not null
   * @param fudgeContext
   *          a Fudge context, not null
   */
  public FieldFilter(final Collection<String> fieldsToAccept, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fieldsToAccept, "fieldsToAccept");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fieldsToAccept = new HashSet<>(fieldsToAccept);
    _context = fudgeContext;
  }

  /**
   * @return the context
   */
  public FudgeContext getContext() {
    return _context;
  }

  @Override
  public MutableFudgeMsg apply(final MutableFudgeMsg msg, final String securityUniqueId, final FieldHistoryStore fieldHistory) {

    final MutableFudgeMsg normalizedMsg = getContext().newMessage();
    // REVIEW kirk 2010-04-15 -- Run through the fields in the order of the
    // original message and check for containment in _fieldsToAccept as it's
    // faster for large messages.
    // It also supports multiple values with the same name.
    for (final FudgeField field : msg) {
      if (field.getName() == null) {
        // Don't allow non-named fields.
        continue;
      }
      if (!_fieldsToAccept.contains(field.getName())) {
        continue;
      }
      normalizedMsg.add(field);
    }

    if (normalizedMsg.getAllFields().isEmpty()) {
      return null; // extinguish message
    }

    return normalizedMsg;
  }

}
