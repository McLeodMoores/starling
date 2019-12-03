/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.legalentity.Account;
import com.opengamma.core.legalentity.Capability;
import com.opengamma.core.legalentity.Obligation;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.legalentity.RootPortfolio;
import com.opengamma.core.legalentity.SeniorityLevel;
import com.opengamma.core.obligor.CreditRating;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.legalentity.ManageableLegalEntity.Meta;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ManageableLegalEntity}.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableLegalEntityTest extends AbstractFudgeBuilderTestCase {
  private static final UniqueId UID = UniqueId.of("uid", "u");
  private static final ExternalIdBundle EID = ExternalIdBundle.of("eid", "e");
  private static final Map<String, String> ATTRIBUTES = Collections.singletonMap("attr", "a");
  private static final Map<String, String> DETAILS = Collections.singletonMap("det", "d");
  private static final String NAME = "name";
  private static final List<Rating> RATINGS = Arrays.asList(new Rating("r1", CreditRating.A, SeniorityLevel.LIEN1),
      new Rating("r2", CreditRating.AA, SeniorityLevel.LIEN2));
  private static final List<Capability> CAPABILITIES = Arrays.asList(new Capability("c1"), new Capability("c2"));
  private static final List<ExternalIdBundle> ISSUED_SECURITIES = Arrays.asList(ExternalIdBundle.of("sec", "s1"), ExternalIdBundle.of("sec", "s2"));
  private static final List<Obligation> OBLIGATIONS = Arrays.asList(new Obligation());
  private static final RootPortfolio ROOT_PORTFOLIO = new RootPortfolio();
  static {
    ROOT_PORTFOLIO.setPortfolio(ObjectId.of("port", "p"));
  }
  private static final List<Account> ACCOUNTS = Arrays.asList(new Account());

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNameConstructor1() {
    new ManageableLegalEntity(null, EID);
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNameConstructor2() {
    new ManageableLegalEntity(UID, null, EID);
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNameSetter() {
    new ManageableLegalEntity().setName(null);
  }
  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExternalIdBundleConstructor1() {
    new ManageableLegalEntity(NAME, null);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExternalIdBundleConstructor2() {
    new ManageableLegalEntity(UID, NAME, null);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExternalIdBundleSetter() {
    new ManageableLegalEntity().setExternalIdBundle(null);
  }

  /**
   * Tests that the attributes cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAttributesSetter() {
    new ManageableLegalEntity().setAttributes(null);
  }

  /**
   * Tests that the details map cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDetailsSetter() {
    new ManageableLegalEntity().setDetails(null);
  }

  /**
   * Tests that the ratings cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRatingsSetter() {
    new ManageableLegalEntity().setRatings(null);
  }

  /**
   * Tests that the capability cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCapabilitiesSetter() {
    new ManageableLegalEntity().setCapabilities(null);
  }

  /**
   * Tests that the issued securities cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIssuedSecuritiesSetter() {
    new ManageableLegalEntity().setIssuedSecurities(null);
  }

  /**
   * Tests that the obligations cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObligationsSetter() {
    new ManageableLegalEntity().setObligations(null);
  }

  /**
   * Tests that the root portfolio can be null.
   */
  @Test
  public void testNullRootPortfolioSetter() {
    new ManageableLegalEntity().setRootPortfolio(null);
  }

  /**
   * Tests that the accounts cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccountsSetter() {
    new ManageableLegalEntity().setAccounts(null);
  }

  /**
   * Tests that an attribute key cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAttributeKey() {
    new ManageableLegalEntity().addAttribute(null, "v");
  }

  /**
   * Tests that an attribute value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAttributeValue() {
    new ManageableLegalEntity().addAttribute("k", null);
  }

  /**
   * Tests that a detail key cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDetailKey() {
    new ManageableLegalEntity().addDetail(null, "v");
  }

  /**
   * Tests that a detail value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDetailValue() {
    new ManageableLegalEntity().addDetail("k", null);
  }

  /**
   * Tests adding an attribute.
   */
  public void testAddAttribute() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    assertTrue(entity.getAttributes().isEmpty());
    entity.addAttribute("k1", "v1");
    assertEquals(entity.getAttributes().size(), 1);
    entity.addAttribute("k2", "v2");
    assertEquals(entity.getAttributes().size(), 2);
  }

  /**
   * Tests adding a detail.
   */
  public void testAddDetails() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    assertTrue(entity.getDetails().isEmpty());
    entity.addDetail("k1", "v1");
    assertEquals(entity.getDetails().size(), 1);
    entity.addDetail("k2", "v2");
    assertEquals(entity.getDetails().size(), 2);
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    setFields(entity);
    final ManageableLegalEntity other = new ManageableLegalEntity();
    setFields(other);
    assertEquals(entity, entity);
    assertEquals(entity.toString(),
        "ManageableLegalEntity{uniqueId=uid~u, externalIdBundle=Bundle[eid~e], attributes={attr=a}, details={det=d}, "
            + "name=name, ratings=[Rating{rater=r1, score=A, seniorityLevel=LIEN1}, Rating{rater=r2, score=AA, seniorityLevel=LIEN2}], "
            + "capabilities=[Capability{name=c1}, Capability{name=c2}], issuedSecurities=[Bundle[sec~s1], Bundle[sec~s2]], "
            + "obligations=[Obligation{name=null, security=null}], rootPortfolio=RootPortfolio{portfolio=port~p}, "
            + "accounts=[Account{name=null, portfolio=null}]}");
    assertEquals(entity, other);
    assertEquals(entity.hashCode(), other.hashCode());
    other.setAccounts(Collections.<Account> emptyList());
    assertNotEquals(entity, other);
    setFields(other);
    other.setAttributes(Collections.<String, String> emptyMap());
    assertNotEquals(entity, other);
    setFields(other);
    other.setCapabilities(Collections.<Capability> emptyList());
    assertNotEquals(entity, other);
    setFields(other);
    other.setDetails(Collections.<String, String> emptyMap());
    assertNotEquals(entity, other);
    setFields(other);
    other.setExternalIdBundle(ExternalIdBundle.EMPTY);
    assertNotEquals(entity, other);
    setFields(other);
    other.setIssuedSecurities(Collections.<ExternalIdBundle> emptyList());
    assertNotEquals(entity, other);
    setFields(other);
    other.setName("other");
    assertNotEquals(entity, other);
    setFields(other);
    other.setObligations(Collections.<Obligation> emptyList());
    assertNotEquals(entity, other);
    setFields(other);
    other.setRatings(Collections.<Rating> emptyList());
    assertNotEquals(entity, other);
    setFields(other);
    other.setRootPortfolio(new RootPortfolio());
    assertNotEquals(entity, other);
    setFields(other);
    other.setUniqueId(UniqueId.of("uid", "u1"));
    assertNotEquals(entity, other);
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    setFields(entity);
    assertEquals(entity.propertyNames().size(), 11);
    final Meta bean = entity.metaBean();
    assertEquals(bean.accounts().get(entity), ACCOUNTS);
    assertEquals(bean.attributes().get(entity), ATTRIBUTES);
    assertEquals(bean.capabilities().get(entity), CAPABILITIES);
    assertEquals(bean.details().get(entity), DETAILS);
    assertEquals(bean.externalIdBundle().get(entity), EID);
    assertEquals(bean.issuedSecurities().get(entity), ISSUED_SECURITIES);
    assertEquals(bean.name().get(entity), NAME);
    assertEquals(bean.obligations().get(entity), OBLIGATIONS);
    assertEquals(bean.ratings().get(entity), RATINGS);
    assertEquals(bean.rootPortfolio().get(entity), ROOT_PORTFOLIO);
    assertEquals(bean.uniqueId().get(entity), UID);
    assertEquals(entity.property("accounts").get(), ACCOUNTS);
    assertEquals(entity.property("attributes").get(), ATTRIBUTES);
    assertEquals(entity.property("capabilities").get(), CAPABILITIES);
    assertEquals(entity.property("details").get(), DETAILS);
    assertEquals(entity.property("externalIdBundle").get(), EID);
    assertEquals(entity.property("issuedSecurities").get(), ISSUED_SECURITIES);
    assertEquals(entity.property("name").get(), NAME);
    assertEquals(entity.property("obligations").get(), OBLIGATIONS);
    assertEquals(entity.property("ratings").get(), RATINGS);
    assertEquals(entity.property("rootPortfolio").get(), ROOT_PORTFOLIO);
    assertEquals(entity.property("uniqueId").get(), UID);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    setFields(entity);
    assertEncodeDecodeCycle(ManageableLegalEntity.class, entity);
  }

  private static void setFields(final ManageableLegalEntity entity) {
    entity.setAccounts(ACCOUNTS);
    entity.setAttributes(ATTRIBUTES);
    entity.setCapabilities(CAPABILITIES);
    entity.setDetails(DETAILS);
    entity.setExternalIdBundle(EID);
    entity.setIssuedSecurities(ISSUED_SECURITIES);
    entity.setName(NAME);
    entity.setObligations(OBLIGATIONS);
    entity.setRatings(RATINGS);
    entity.setRootPortfolio(ROOT_PORTFOLIO);
    entity.setUniqueId(UID);
  }
}
