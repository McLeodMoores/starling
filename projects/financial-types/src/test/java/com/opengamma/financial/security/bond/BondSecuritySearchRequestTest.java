/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.bond;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for {@link BondSecuritySearchRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class BondSecuritySearchRequestTest extends AbstractBeanTestCase {
  private static final String ISSUER_NAME = "govt issuer name";
  private static final String ISSUER_TYPE = "govt issuer type";
  private static final BondSecurity BOND = new GovernmentBondSecurity(ISSUER_NAME, ISSUER_TYPE, "US", "GOVT", Currency.USD, SimpleYieldConvention.US_STREET,
      new Expiry(DateUtils.getUTCDate(2029, 12, 15)), "Fixed", 0.003, SimpleFrequency.SEMI_ANNUAL, DayCounts.ACT_360, DateUtils.getUTCDate(2019, 12, 15),
      DateUtils.getUTCDate(2019, 12, 15), DateUtils.getUTCDate(2019, 3, 15), 100d, 10000d, 10d, 1000d, 100d, 100d);

  /**
   * Tests that a document with the wrong name will not match.
   */
  public void testWrongNameNoMatch() {
    final BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    final SecurityDocument doc = new SecurityDocument(BOND);
    doc.getSecurity().setName("bond");
    request.setName("government bond");
    assertFalse(request.matches(doc));
  }

  /**
   * Tests that a document with the wrong security type will not match.
   */
  public void testWrongSecurityTypeNoMatch() {
    final EquitySecurity equity = new EquitySecurity("exch", "code", "co", Currency.USD);
    final BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    final SecurityDocument doc = new SecurityDocument(equity);
    assertFalse(request.matches(doc));
  }

  /**
   * Tests that a document with the wrong issuer name will not match.
   */
  public void testWrongIssuerNameNoMatch() {
    final BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    request.setIssuerName(ISSUER_TYPE);
    final SecurityDocument doc = new SecurityDocument(BOND);
    assertFalse(request.matches(doc));
  }

  /**
   * Tests that a document with the wrong issuer name will not match.
   */
  public void testWrongIssuerNameWildcardNoMatch() {
    final BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    request.setIssuerName("*Z*");
    final SecurityDocument doc = new SecurityDocument(BOND);
    assertFalse(request.matches(doc));
  }

  /**
   * Tests that a document with the wrong issuer type will not match.
   */
  public void testWrongIssuerTypeNoMatch() {
    final BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    request.setIssuerType(ISSUER_NAME);
    final SecurityDocument doc = new SecurityDocument(BOND);
    assertFalse(request.matches(doc));
  }

  /**
   * Tests that a document with the wrong issuer type will not match.
   */
  public void testWrongIssuerTypeWildcardNoMatch() {
    final BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    request.setIssuerType("*Z*");
    final SecurityDocument doc = new SecurityDocument(BOND);
    assertFalse(request.matches(doc));
  }

  /**
   * Tests a matching document.
   */
  public void testMatchingIssuerName() {
    final BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    request.setIssuerName(ISSUER_NAME);
    final SecurityDocument doc = new SecurityDocument(BOND);
    assertTrue(request.matches(doc));
  }

  /**
   * Tests a matching document.
   */
  public void testMatchingIssuerNameWildcard() {
    final BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    request.setIssuerName("*i*");
    final SecurityDocument doc = new SecurityDocument(BOND);
    assertTrue(request.matches(doc));
  }

  /**
   * Tests a matching document.
   */
  public void testMatchingIssuerType() {
    final BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    request.setIssuerType(ISSUER_TYPE);
    final SecurityDocument doc = new SecurityDocument(BOND);
    assertTrue(request.matches(doc));
  }

  /**
   * Tests a matching document.
   */
  public void testMatchingIssuerTypeWildCard() {
    final BondSecuritySearchRequest request = new BondSecuritySearchRequest();
    request.setIssuerName("*i*");
    final SecurityDocument doc = new SecurityDocument(BOND);
    assertTrue(request.matches(doc));
  }

  @Override
  public JodaBeanProperties<BondSecuritySearchRequest> getJodaBeanProperties() {
    return new JodaBeanProperties<>(BondSecuritySearchRequest.class, Arrays.asList("issuerName", "issuerType"),
        Arrays.<Object> asList(ISSUER_NAME, ISSUER_TYPE), Arrays.<Object> asList(ISSUER_TYPE, ISSUER_NAME));
  }

}