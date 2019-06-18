/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.normalization;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecurityRuleApplier}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityRuleApplierTest {
  private static final MutableFudgeMsg MSG = OpenGammaFudgeContext.getInstance().newMessage();
  static {
    MSG.add(MarketDataRequirementNames.LAST, 178);
  }

  /**
   * Tests that the rule provider cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRuleProvider() {
    new SecurityRuleApplier(null);
  }

  /**
   * Tests the case where the rule provider returns null if there is no rule for
   * a unique id.
   */
  public void testNullProvider() {
    final SecurityRuleProvider rule = new SecurityRuleProvider() {

      @Override
      public NormalizationRule getRule(final String securityUniqueId) {
        return null;
      }
    };
    final SecurityRuleApplier applier = new SecurityRuleApplier(rule);
    assertSame(applier.apply(MSG, "uid", new FieldHistoryStore()), MSG);
  }

  /**
   * Tests the case where the rule provider throws an exception if there is no
   * rule for a unique id.
   */
  public void testExceptionProvider() {
    final SecurityRuleProvider rule = new SecurityRuleProvider() {

      @Override
      public NormalizationRule getRule(final String securityUniqueId) {
        throw new OpenGammaRuntimeException("");
      }
    };
    final SecurityRuleApplier applier = new SecurityRuleApplier(rule);
    assertNull(applier.apply(MSG, "uid", new FieldHistoryStore()));
  }

  /**
   * Tests the case where the rule throws an exception.
   */
  public void testExceptionRule() {
    final SecurityRuleProvider rule = new SecurityRuleProvider() {

      @Override
      public NormalizationRule getRule(final String securityUniqueId) {
        return new NormalizationRule() {

          @Override
          public MutableFudgeMsg apply(final MutableFudgeMsg msg, final String uid, final FieldHistoryStore fieldHistory) {
            throw new OpenGammaRuntimeException("");
          }
        };
      }
    };
    final SecurityRuleApplier applier = new SecurityRuleApplier(rule);
    assertNull(applier.apply(MSG, "uid", new FieldHistoryStore()));
  }

  /**
   * Tests the rule applier.
   */
  public void testRule() {
    final String uid = "uid";
    final NormalizationRule rule = new NormalizationRule() {
      @Override
      public MutableFudgeMsg apply(final MutableFudgeMsg msg, final String securityUniqueId, final FieldHistoryStore fieldHistory) {
        final MutableFudgeMsg result = OpenGammaFudgeContext.getInstance().newMessage();
        for (final FudgeField field : msg) {
          result.add(field);
        }
        final FudgeField last = result.getByName(MarketDataRequirementNames.LAST);
        if (last != null && uid.equals(securityUniqueId)) {
          final double newValue = ((Number) last.getValue()).doubleValue() * 100;
          result.remove(MarketDataRequirementNames.LAST);
          result.add(MarketDataRequirementNames.LAST, newValue);
        }
        return result;
      }
    };
    final SecurityRuleProvider provider = new SecurityRuleProvider() {

      @Override
      public NormalizationRule getRule(final String securityUniqueId) {
        return rule;
      }

    };
    final SecurityRuleApplier applier = new SecurityRuleApplier(provider);
    final MutableFudgeMsg applied1 = applier.apply(MSG, uid, new FieldHistoryStore());
    final MutableFudgeMsg applied2 = applier.apply(MSG, uid + "1", new FieldHistoryStore());
    assertEquals(applied1.getAllFields().size(), 1);
    assertEquals(applied2.getAllFields().size(), 1);
    assertEquals(applied1.getDouble(MarketDataRequirementNames.LAST), 17800.);
    assertEquals(applied2.getDouble(MarketDataRequirementNames.LAST), 178.);
  }
}
