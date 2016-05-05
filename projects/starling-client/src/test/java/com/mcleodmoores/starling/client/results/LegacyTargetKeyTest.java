/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link LegacyTargetKey}.
 */
@Test(groups = TestGroup.UNIT)
public class LegacyTargetKeyTest {

  /**
   * Tests the behaviour with a null computation target specification.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfNull() {
    LegacyTargetKey.of(null);
  }

  /**
   * Tests key creation.
   */
  @Test
  public void testOf() {
    assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.NULL));
    assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)));
    assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))));
    final SimplePortfolioNode portfolioNode = new SimplePortfolioNode();
    portfolioNode.setUniqueId(UniqueId.of("A", "B"));
    assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(portfolioNode)));
    final SimplePosition position = new SimplePosition();
    position.setUniqueId(UniqueId.of("A", "B"));
    assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(position)));
    final CashFlowSecurity security = new CashFlowSecurity(Currency.AUD, ZonedDateTime.now(), 1234);
    security.setUniqueId(UniqueId.of("A", "B"));
    assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(security)));
    assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))));
  }

  /**
   * Tests the hashcode.
   */
  @Test
  public void testHashCode() {
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.NULL).hashCode(), LegacyTargetKey.of(ComputationTargetSpecification.NULL).hashCode());
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)).hashCode(),
        LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)).hashCode());
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))).hashCode(),
        LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))).hashCode());
    final SimplePortfolioNode portfolioNode = new SimplePortfolioNode();
    portfolioNode.setUniqueId(UniqueId.of("A", "B"));
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(portfolioNode)).hashCode(), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(portfolioNode)).hashCode());
    final SimplePosition position = new SimplePosition();
    position.setUniqueId(UniqueId.of("A", "B"));
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(position)).hashCode(), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(position)).hashCode());
    final CashFlowSecurity security = new CashFlowSecurity(Currency.AUD, ZonedDateTime.now(), 1234);
    security.setUniqueId(UniqueId.of("A", "B"));
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(security)).hashCode(), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(security)).hashCode());
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))).hashCode(), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final LegacyTargetKey nullSpecificationTargetKey = LegacyTargetKey.of(ComputationTargetSpecification.NULL);
    assertEquals(nullSpecificationTargetKey, nullSpecificationTargetKey);
    assertEquals(nullSpecificationTargetKey, LegacyTargetKey.of(ComputationTargetSpecification.NULL));
    assertNotEquals(null, nullSpecificationTargetKey);
    assertNotEquals(new Object(), nullSpecificationTargetKey);
    assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)), nullSpecificationTargetKey);
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)));
    assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.USD)));
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))));
    assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCDEF"))));
    final SimplePortfolioNode portfolioNode1 = new SimplePortfolioNode();
    portfolioNode1.setUniqueId(UniqueId.of("A", "B"));
    final SimplePortfolioNode portfolioNode2 = new SimplePortfolioNode("Hello");
    portfolioNode2.setUniqueId(UniqueId.of("A", "C"));
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(portfolioNode1)), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(portfolioNode1)));
    assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(portfolioNode1)), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(portfolioNode2)));
    final SimplePosition position1 = new SimplePosition();
    position1.setUniqueId(UniqueId.of("A", "B"));
    final SimplePosition position2 = new SimplePosition(BigDecimal.TEN, ExternalId.of("A", "B"));
    position2.setUniqueId(UniqueId.of("A", "C"));
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(position1)), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(position1)));
    assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(position1)), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(position2)));
    final ZonedDateTime now = ZonedDateTime.now(); // in case test runs over day boundary.
    final CashFlowSecurity security1 = new CashFlowSecurity(Currency.AUD, now, 1234);
    security1.setUniqueId(UniqueId.of("A", "B"));
    final CashFlowSecurity security2 = new CashFlowSecurity(Currency.AUD, now.plusHours(1), 1234);
    security2.setUniqueId(UniqueId.of("A", "C"));
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(security1)), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(security1)));
    assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(security1)), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(security2)));
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))));
    assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))), 
        LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "C"))));
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.NULL).toString(), 
        "LegacyTargetKey[targetSpecification=CTSpec[NULL, NULL]]");
    assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.USD)).toString(), 
        "LegacyTargetKey[targetSpecification=CTSpec[CURRENCY, CurrencyISO~USD]]");
  }
}