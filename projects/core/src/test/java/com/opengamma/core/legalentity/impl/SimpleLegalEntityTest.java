/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.legalentity.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.core.legalentity.Account;
import com.opengamma.core.legalentity.Capability;
import com.opengamma.core.legalentity.Obligation;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.legalentity.RootPortfolio;
import com.opengamma.core.legalentity.SeniorityLevel;
import com.opengamma.core.obligor.CreditRating;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.test.Assert;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SimpleLegalEntity}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleLegalEntityTest extends AbstractFudgeBuilderTestCase {
  private static final ExternalIdBundle ID_BUNDLE = ExternalIdBundle.of(
      ExternalId.of("eid", "1"), ExternalId.of("eid", "2"));
  private static final Map<String, String> ATTRIBUTES = Collections.singletonMap("attr", "val");
  private static final Map<String, String> DETAILS = Collections.singletonMap("det", "val");
  private static final String NAME = "name";
  private static final List<Rating> RATINGS = Arrays.asList(
      new Rating("A", CreditRating.A, SeniorityLevel.LIEN1), new Rating("A", CreditRating.B, SeniorityLevel.LIEN2));
  private static final List<Capability> CAPABILITIES = Arrays.asList(
      new Capability("cap"));
  private static final List<ExternalIdBundle> ISSUED_SECURITIES = Arrays.asList(
      ExternalIdBundle.of("eid", "3"), ExternalIdBundle.of("eid", "4"));
  private static final List<Obligation> OBLIGATIONS = Arrays.asList(
      new Obligation());
  private static final RootPortfolio ROOT_PORTFOLIO = new RootPortfolio();
  static {
    ROOT_PORTFOLIO.setPortfolio(ObjectId.of("oid", "1"));
  }
  private static final List<Account> ACCOUNTS = Arrays.asList(
      new Account());
  private static final UniqueId UID = UniqueId.of("uid", "100", "1000");
  private static final SimpleLegalEntity LE = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
  static {
    LE.setAccounts(ACCOUNTS);
    LE.setAttributes(ATTRIBUTES);
    LE.setCapabilities(CAPABILITIES);
    LE.setDetails(DETAILS);
    LE.setIssuedSecurities(ISSUED_SECURITIES);
    LE.setObligations(OBLIGATIONS);
    LE.setRatings(RATINGS);
    LE.setRootPortfolio(ROOT_PORTFOLIO);
  }

  /**
   * Tests constructor equivalence.
   */
  @Test
  public void testConstructor() {
    final SimpleLegalEntity le1 = new SimpleLegalEntity();
    le1.setName(NAME);
    le1.setExternalIdBundle(ID_BUNDLE);
    le1.setUniqueId(UID);
    final SimpleLegalEntity le2 = new SimpleLegalEntity(NAME, ID_BUNDLE);
    le2.setUniqueId(UID);
    final SimpleLegalEntity le3 = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
    assertEquals(le1, le2);
    assertEquals(le1, le3);
  }

  /**
   * Tests adding an external id.
   */
  @Test
  public void testAddExternalId() {
    final SimpleLegalEntity le = LE.clone();
    assertEquals(le.getExternalIdBundle(), ID_BUNDLE);
    le.addExternalId(ExternalId.of("eid", "9"));
    assertEquals(le.getExternalIdBundle().size(), 3);
    final Set<String> ids = le.getExternalIdBundle().getValues(ExternalScheme.of("eid"));
    Assert.assertEqualsNoOrder(ids, Arrays.asList("1", "2", "9"));
  }

  /**
   * Tests adding an attribute.
   */
  @Test
  public void testAddAttribute() {
    final SimpleLegalEntity le = LE.clone();
    assertEquals(le.getAttributes(), ATTRIBUTES);
    le.addAttribute("y", "z");
    assertEquals(le.getAttributes().size(), 2);
    Assert.assertEqualsNoOrder(le.getAttributes().keySet(), Arrays.asList("attr", "y"));
    Assert.assertEqualsNoOrder(le.getAttributes().values(), Arrays.asList("val", "z"));
  }

  /**
   * Tests adding a detail.
   */
  @Test
  public void testAddDetail() {
    final SimpleLegalEntity le = LE.clone();
    assertEquals(le.getDetails(), DETAILS);
    le.addDetail("a", "b");
    assertEquals(le.getDetails().size(), 2);
    Assert.assertEqualsNoOrder(le.getDetails().keySet(), Arrays.asList("det", "a"));
    Assert.assertEqualsNoOrder(le.getDetails().values(), Arrays.asList("val", "b"));
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(SimpleLegalEntity.class, LE);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(LE.metaBean());
    assertEquals(LE.metaBean().accounts().get(LE), ACCOUNTS);
    assertEquals(LE.metaBean().metaProperty("accounts").get(LE), ACCOUNTS);
    assertEquals(LE.property("accounts").get(), ACCOUNTS);
    assertEquals(LE.metaBean().attributes().get(LE), ATTRIBUTES);
    assertEquals(LE.metaBean().metaProperty("attributes").get(LE), ATTRIBUTES);
    assertEquals(LE.property("attributes").get(), ATTRIBUTES);
    assertEquals(LE.metaBean().capabilities().get(LE), CAPABILITIES);
    assertEquals(LE.metaBean().metaProperty("capabilities").get(LE), CAPABILITIES);
    assertEquals(LE.property("capabilities").get(), CAPABILITIES);
    assertEquals(LE.metaBean().details().get(LE), DETAILS);
    assertEquals(LE.metaBean().metaProperty("details").get(LE), DETAILS);
    assertEquals(LE.property("details").get(), DETAILS);
    assertEquals(LE.metaBean().externalIdBundle().get(LE), ID_BUNDLE);
    assertEquals(LE.metaBean().metaProperty("externalIdBundle").get(LE), ID_BUNDLE);
    assertEquals(LE.property("externalIdBundle").get(), ID_BUNDLE);
    assertEquals(LE.metaBean().issuedSecurities().get(LE), ISSUED_SECURITIES);
    assertEquals(LE.metaBean().metaProperty("issuedSecurities").get(LE), ISSUED_SECURITIES);
    assertEquals(LE.property("issuedSecurities").get(), ISSUED_SECURITIES);
    assertEquals(LE.metaBean().name().get(LE), NAME);
    assertEquals(LE.metaBean().metaProperty("name").get(LE), NAME);
    assertEquals(LE.property("name").get(), NAME);
    assertEquals(LE.metaBean().obligations().get(LE), OBLIGATIONS);
    assertEquals(LE.metaBean().metaProperty("obligations").get(LE), OBLIGATIONS);
    assertEquals(LE.property("obligations").get(), OBLIGATIONS);
    assertEquals(LE.metaBean().ratings().get(LE), RATINGS);
    assertEquals(LE.metaBean().metaProperty("ratings").get(LE), RATINGS);
    assertEquals(LE.property("ratings").get(), RATINGS);
    assertEquals(LE.metaBean().rootPortfolio().get(LE), ROOT_PORTFOLIO);
    assertEquals(LE.metaBean().metaProperty("rootPortfolio").get(LE), ROOT_PORTFOLIO);
    assertEquals(LE.property("rootPortfolio").get(), ROOT_PORTFOLIO);
    assertEquals(LE.metaBean().uniqueId().get(LE), UID);
    assertEquals(LE.metaBean().metaProperty("uniqueId").get(LE), UID);
    assertEquals(LE.property("uniqueId").get(), UID);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(LE, LE);
    assertNotEquals(null, LE);
    assertNotEquals(ID_BUNDLE, LE);
    SimpleLegalEntity other = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
    other.setAccounts(ACCOUNTS);
    other.setAttributes(ATTRIBUTES);
    other.setCapabilities(CAPABILITIES);
    other.setDetails(DETAILS);
    other.setIssuedSecurities(ISSUED_SECURITIES);
    other.setObligations(OBLIGATIONS);
    other.setRatings(RATINGS);
    other.setRootPortfolio(ROOT_PORTFOLIO);
    assertEquals(LE, other);
    assertEquals(LE.hashCode(), other.hashCode());
    other = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
    other.setAttributes(ATTRIBUTES);
    other.setCapabilities(CAPABILITIES);
    other.setDetails(DETAILS);
    other.setIssuedSecurities(ISSUED_SECURITIES);
    other.setObligations(OBLIGATIONS);
    other.setRatings(RATINGS);
    other.setRootPortfolio(ROOT_PORTFOLIO);
    assertNotEquals(other, LE);
    other = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
    other.setAccounts(ACCOUNTS);
    other.setCapabilities(CAPABILITIES);
    other.setDetails(DETAILS);
    other.setIssuedSecurities(ISSUED_SECURITIES);
    other.setObligations(OBLIGATIONS);
    other.setRatings(RATINGS);
    other.setRootPortfolio(ROOT_PORTFOLIO);
    assertNotEquals(other, LE);
    other = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
    other.setAccounts(ACCOUNTS);
    other.setAttributes(ATTRIBUTES);
    other.setDetails(DETAILS);
    other.setIssuedSecurities(ISSUED_SECURITIES);
    other.setObligations(OBLIGATIONS);
    other.setRatings(RATINGS);
    other.setRootPortfolio(ROOT_PORTFOLIO);
    assertNotEquals(other, LE);
    other = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
    other.setAccounts(ACCOUNTS);
    other.setAttributes(ATTRIBUTES);
    other.setCapabilities(CAPABILITIES);
    other.setIssuedSecurities(ISSUED_SECURITIES);
    other.setObligations(OBLIGATIONS);
    other.setRatings(RATINGS);
    other.setRootPortfolio(ROOT_PORTFOLIO);
    assertNotEquals(other, LE);
    other = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
    other.setAccounts(ACCOUNTS);
    other.setAttributes(ATTRIBUTES);
    other.setCapabilities(CAPABILITIES);
    other.setDetails(DETAILS);
    other.setObligations(OBLIGATIONS);
    other.setRatings(RATINGS);
    other.setRootPortfolio(ROOT_PORTFOLIO);
    assertNotEquals(other, LE);
    other = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
    other.setAccounts(ACCOUNTS);
    other.setAttributes(ATTRIBUTES);
    other.setCapabilities(CAPABILITIES);
    other.setDetails(DETAILS);
    other.setIssuedSecurities(ISSUED_SECURITIES);
    other.setRatings(RATINGS);
    other.setRootPortfolio(ROOT_PORTFOLIO);
    assertNotEquals(other, LE);
    other = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
    other.setAccounts(ACCOUNTS);
    other.setAttributes(ATTRIBUTES);
    other.setCapabilities(CAPABILITIES);
    other.setDetails(DETAILS);
    other.setIssuedSecurities(ISSUED_SECURITIES);
    other.setObligations(OBLIGATIONS);
    other.setRootPortfolio(ROOT_PORTFOLIO);
    assertNotEquals(other, LE);
    other = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
    other.setAccounts(ACCOUNTS);
    other.setAttributes(ATTRIBUTES);
    other.setCapabilities(CAPABILITIES);
    other.setDetails(DETAILS);
    other.setIssuedSecurities(ISSUED_SECURITIES);
    other.setObligations(OBLIGATIONS);
    other.setRatings(RATINGS);
    assertNotEquals(other, LE);
    other = new SimpleLegalEntity(UID, NAME, ID_BUNDLE);
    other.setAccounts(ACCOUNTS);
    other.setAttributes(ATTRIBUTES);
    other.setCapabilities(CAPABILITIES);
    other.setDetails(DETAILS);
    other.setIssuedSecurities(ISSUED_SECURITIES);
    other.setObligations(OBLIGATIONS);
    other.setRatings(RATINGS);
    assertNotEquals(other, LE);
  }
}
