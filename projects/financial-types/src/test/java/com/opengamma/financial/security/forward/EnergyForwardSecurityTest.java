/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.forward;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for {@link EnergyForwardSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class EnergyForwardSecurityTest extends AbstractBeanTestCase {
  private static final String UNIT_NAME = "100";
  private static final Double UNIT_NUMBER = 100.;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 3, 15));
  private static final Currency CCY = Currency.AUD;
  private static final double UNIT_AMOUNT = 25;
  private static final String CONTRACT_CATEGORY = "cat";
  private static final EnergyForwardSecurity FORWARD = new EnergyForwardSecurity(UNIT_NAME, UNIT_NUMBER, EXPIRY, CCY, UNIT_AMOUNT, CONTRACT_CATEGORY);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(EnergyForwardSecurity.class,
        Arrays.asList("unitName", "unitNumber", "expiry", "currency", "unitAmount", "contractCategory"),
        Arrays.asList(UNIT_NAME, UNIT_NUMBER, EXPIRY, CCY, UNIT_AMOUNT, CONTRACT_CATEGORY),
        Arrays.asList("10", UNIT_NUMBER * 2, new Expiry(DateUtils.getUTCDate(2020, 6, 15)), Currency.BRL, UNIT_AMOUNT * 2, "con"));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  @Test
  public void testConstructor() {
    final EnergyForwardSecurity forward = new EnergyForwardSecurity(UNIT_NAME, UNIT_NUMBER, EXPIRY, CCY, UNIT_AMOUNT, CONTRACT_CATEGORY);
    assertEquals(forward.getUnitName(), UNIT_NAME);
    assertEquals(forward.getUnitNumber(), UNIT_NUMBER);
    assertEquals(forward.getExpiry(), EXPIRY);
    assertEquals(forward.getCurrency(), CCY);
    assertEquals(forward.getUnitAmount(), UNIT_AMOUNT);
    assertEquals(forward.getContractCategory(), CONTRACT_CATEGORY);
  }

  /**
   * Tests the security type.
   */
  public void testSecurityType() {
    assertEquals(FORWARD.getSecurityType(), CommodityForwardSecurity.SECURITY_TYPE);
  }

  /**
   * Tests that the accept() method points to the right method in the visitor.
   */
  public void testAcceptVisitor() {
    assertEquals(FORWARD.accept(TestVisitor.INSTANCE), "visited");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
      return "visited";
    }

  }
}
