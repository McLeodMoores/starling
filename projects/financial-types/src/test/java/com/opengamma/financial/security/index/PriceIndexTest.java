/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.index;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the fields of the price index. This test is intended to pick up any
 * changes to the price index before databases are affected.
 */
@Test(groups = TestGroup.UNIT)
public class PriceIndexTest extends AbstractBeanTestCase {
  /** The index name */
  private static final String NAME = "UK RPI";
  /** The index description */
  private static final String DESCRIPTION = "PRICE DESCRIPTION";
  /** The tickers */
  private static final ExternalIdBundle TICKERS = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("UKRPI INDEX"),
      ExternalSchemes.syntheticSecurityId(NAME));
  /** The convention id */
  private static final ExternalId CONVENTION_ID = ExternalId.of("SCHEME", "USD PRICE CONVENTION");
  /** The index */
  private static final PriceIndex INDEX_NO_DESCRIPTION = new PriceIndex(NAME, CONVENTION_ID);
  /** The index */
  private static final PriceIndex INDEX_WITH_DESCRIPTION = new PriceIndex(NAME, DESCRIPTION, CONVENTION_ID);

  static {
    INDEX_NO_DESCRIPTION.setExternalIdBundle(TICKERS);
    INDEX_WITH_DESCRIPTION.setExternalIdBundle(TICKERS);
  }
  /**
   * Tests that the convention id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionId() {
    new PriceIndex(NAME, DESCRIPTION, null);
  }

  /**
   * Tests the number of fields in the index. Will pick up additions / removals.
   */
  @Test
  public void testNumberOfFields() {
    List<Field> fields = IndexTestUtils.getFields(INDEX_NO_DESCRIPTION.getClass());
    assertEquals(14, fields.size());
    fields = IndexTestUtils.getFields(INDEX_WITH_DESCRIPTION.getClass());
    assertEquals(14, fields.size());
  }

  /**
   * Tests that fields are set correctly and that fields that should be null are.
   */
  @Test
  public void test() {
    assertEquals(NAME, INDEX_NO_DESCRIPTION.getName());
    assertEquals(NAME, INDEX_WITH_DESCRIPTION.getName());
    assertNull(INDEX_NO_DESCRIPTION.getDescription());
    assertEquals(DESCRIPTION, INDEX_WITH_DESCRIPTION.getDescription());
    assertEquals(TICKERS, INDEX_NO_DESCRIPTION.getExternalIdBundle());
    assertEquals(TICKERS, INDEX_WITH_DESCRIPTION.getExternalIdBundle());
    assertEquals(CONVENTION_ID, INDEX_NO_DESCRIPTION.getConventionId());
    assertEquals(CONVENTION_ID, INDEX_WITH_DESCRIPTION.getConventionId());
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(PriceIndex.class, Arrays.asList("securityType", "name", "description", "conventionId"),
        Arrays.asList(PriceIndex.INDEX_TYPE, NAME, DESCRIPTION, CONVENTION_ID),
        Arrays.asList(BondIndex.INDEX_TYPE, "other", "INDEX DESCRIPTION", ExternalId.of("eid", "3")));
  }

}
