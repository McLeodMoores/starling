package com.mcleodmoores.starling.client.results;

import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.money.Currency;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import java.math.BigDecimal;

import static org.testng.Assert.*;

/**
 * Created by jim on 19/06/15.
 */
public class LegacyTargetKeyTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfNull() throws Exception {
    LegacyTargetKey.of(null);
  }

  @Test
  public void testOf() throws Exception {
    Assert.assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.NULL));
    Assert.assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)));
    Assert.assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))));
    Assert.assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePortfolioNode())));
    Assert.assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePosition())));
    Assert.assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(new CashFlowSecurity(Currency.AUD, ZonedDateTime.now(), 1234))));
    Assert.assertNotNull(LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))));
  }

  @Test
  public void testHashCode() throws Exception {
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.NULL).hashCode(), LegacyTargetKey.of(ComputationTargetSpecification.NULL).hashCode());
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)).hashCode(),
        LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)).hashCode());
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))).hashCode(),
        LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))).hashCode());
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePortfolioNode())).hashCode(),
        LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePortfolioNode())).hashCode());
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePosition())).hashCode(),
        LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePosition())).hashCode());
    ZonedDateTime now = ZonedDateTime.now(); // in case test runs over day boundary.
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(new CashFlowSecurity(Currency.AUD, now, 1234))).hashCode(),
        LegacyTargetKey.of(ComputationTargetSpecification.of(new CashFlowSecurity(Currency.AUD, now, 1234))).hashCode());
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))).hashCode(),
        LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))).hashCode());
  }

  @Test
  public void testEquals() throws Exception {
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.NULL), LegacyTargetKey.of(ComputationTargetSpecification.NULL));
    Assert.assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.NULL), null);
    Assert.assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.NULL), new Object());
    Assert.assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.NULL), new Object());
    Assert.assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)), LegacyTargetKey.of(ComputationTargetSpecification.NULL));
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)),
        LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)));
    Assert.assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.AUD)),
        LegacyTargetKey.of(ComputationTargetSpecification.of(Currency.USD)));
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))),
        LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))));
    Assert.assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCD"))),
        LegacyTargetKey.of(ComputationTargetSpecification.of(CreditCurveIdentifier.of("ABCDEF"))));
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePortfolioNode())),
        LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePortfolioNode())));
    Assert.assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePortfolioNode())),
        LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePortfolioNode("Hello"))));
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePosition())),
        LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePosition())));
    Assert.assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePosition())),
        LegacyTargetKey.of(ComputationTargetSpecification.of(new SimplePosition(BigDecimal.TEN, ExternalId.of("A", "B")))));
    ZonedDateTime now = ZonedDateTime.now(); // in case test runs over day boundary.
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(new CashFlowSecurity(Currency.AUD, now, 1234))),
        LegacyTargetKey.of(ComputationTargetSpecification.of(new CashFlowSecurity(Currency.AUD, now, 1234))));
    Assert.assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(new CashFlowSecurity(Currency.AUD, now, 1234))),
        LegacyTargetKey.of(ComputationTargetSpecification.of(new CashFlowSecurity(Currency.AUD, now.plusHours(1), 1234))));
    Assert.assertEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))),
        LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))));
    Assert.assertNotEquals(LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "B"))),
        LegacyTargetKey.of(ComputationTargetSpecification.of(UniqueId.of("A", "C"))));
  }
}