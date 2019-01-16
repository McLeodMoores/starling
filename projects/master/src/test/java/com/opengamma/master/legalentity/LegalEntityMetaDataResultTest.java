/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LegalEntityMetaDataResult}.
 */
@Test(groups = TestGroup.UNIT)
public class LegalEntityMetaDataResultTest extends AbstractFudgeBuilderTestCase {
  private static final String SCHEMA_VERSION = "1.2";

  /**
   * Tests the object.
   */
  public void testObject() {
    final LegalEntityMetaDataResult result = new LegalEntityMetaDataResult();
    result.setSchemaVersion(SCHEMA_VERSION);
    final LegalEntityMetaDataResult other = new LegalEntityMetaDataResult();
    other.setSchemaVersion(SCHEMA_VERSION);
    assertEquals(result, result);
    assertEquals(result.toString(), "LegalEntityMetaDataResult{schemaVersion=1.2}");
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setSchemaVersion(null);
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    final LegalEntityMetaDataResult result = new LegalEntityMetaDataResult();
    result.setSchemaVersion(SCHEMA_VERSION);
    assertEquals(result.propertyNames().size(), 1);
    assertEquals(LegalEntityMetaDataResult.meta().schemaVersion().get(result), SCHEMA_VERSION);
    assertEquals(result.property("schemaVersion").get(), SCHEMA_VERSION);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final LegalEntityMetaDataResult result = new LegalEntityMetaDataResult();
    assertEncodeDecodeCycle(LegalEntityMetaDataResult.class, result);
    result.setSchemaVersion(SCHEMA_VERSION);
    assertEncodeDecodeCycle(LegalEntityMetaDataResult.class, result);
  }
}
