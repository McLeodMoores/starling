/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LegalEntityDocument}.
 */
@Test(groups = TestGroup.UNIT)
public class LegalEntityDocumentTest extends AbstractFudgeBuilderTestCase {
  private static final Instant VERSION = Instant.ofEpochSecond(800);
  private static final Instant CORRECTION = Instant.ofEpochSecond(1600);
  private static final UniqueId UID = UniqueId.of("uid", "1");
  private static final String NAME = "name";
  private static final ManageableLegalEntity ENTITY = new ManageableLegalEntity(UID, NAME, ExternalIdBundle.of("eid", "1"));

  /**
   * Tests that the legal entity cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLegalEntityConstructor() {
    new LegalEntityDocument(null);
  }

  /**
   * Tests that the legal entity cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLegalEntitySetter() {
    new LegalEntityDocument().setLegalEntity(null);
  }

  /**
   * Tests getting the name.
   */
  public void testGetName() {
    assertNull(new LegalEntityDocument().getName());
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    assertEquals(new LegalEntityDocument(entity).getName(), "");
    entity.setName(NAME);
    assertEquals(new LegalEntityDocument(entity).getName(), NAME);
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final LegalEntityDocument doc = new LegalEntityDocument(ENTITY);
    final LegalEntityDocument other = new LegalEntityDocument(ENTITY);
    assertEquals(doc, doc);
    assertEquals(doc.toString(), "LegalEntityDocument{versionFromInstant=null, versionToInstant=null, correctionFromInstant=null, "
        + "correctionToInstant=null, legalEntity=ManageableLegalEntity{uniqueId=uid~1, externalIdBundle=Bundle[eid~1], attributes={}, "
        + "details={}, name=name, ratings=[], capabilities=[], issuedSecurities=[], obligations=[], rootPortfolio=null, accounts=[]}, uniqueId=uid~1}");
    assertEquals(doc, other);
    assertEquals(doc.hashCode(), other.hashCode());
    other.setCorrectionFromInstant(CORRECTION);
    assertNotEquals(doc, other);
    other.setCorrectionFromInstant(null);
    other.setCorrectionToInstant(CORRECTION);
    assertNotEquals(doc, other);
    other.setCorrectionToInstant(null);
    other.setLegalEntity(new ManageableLegalEntity());
    assertNotEquals(doc, other);
    other.setLegalEntity(ENTITY);
    other.setUniqueId(UniqueId.of("uid", "1000"));
    assertNotEquals(doc, other);
    other.setUniqueId(UID);
    other.setVersionFromInstant(VERSION);
    assertNotEquals(doc, other);
    other.setVersionFromInstant(VERSION);
    other.setVersionToInstant(VERSION);
    assertNotEquals(doc, other);
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    final LegalEntityDocument doc = new LegalEntityDocument(ENTITY);
    doc.setCorrectionFromInstant(CORRECTION);
    doc.setVersionToInstant(VERSION);
    assertEquals(doc.propertyNames().size(), 6);
    assertEquals(LegalEntityDocument.meta().correctionFromInstant().get(doc), CORRECTION);
    assertNull(LegalEntityDocument.meta().correctionToInstant().get(doc));
    assertEquals(LegalEntityDocument.meta().legalEntity().get(doc), ENTITY);
    assertEquals(LegalEntityDocument.meta().uniqueId().get(doc), UID);
    assertNull(LegalEntityDocument.meta().versionFromInstant().get(doc));
    assertEquals(LegalEntityDocument.meta().versionToInstant().get(doc), VERSION);
    assertEquals(doc.property("correctionFromInstant").get(), CORRECTION);
    assertNull(doc.property("correctionToInstant").get());
    assertEquals(doc.property("legalEntity").get(), ENTITY);
    assertEquals(doc.property("uniqueId").get(), UID);
    assertNull(doc.property("versionFromInstant").get());
    assertEquals(doc.property("versionToInstant").get(), VERSION);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final LegalEntityDocument doc = new LegalEntityDocument(ENTITY);
    assertEncodeDecodeCycle(LegalEntityDocument.class, doc);
  }
}
