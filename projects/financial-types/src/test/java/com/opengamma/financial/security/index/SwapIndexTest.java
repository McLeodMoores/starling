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
import com.opengamma.util.time.Tenor;

/**
 * Tests the fields of the swap index. This test is intended to pick up any
 * changes to the swap index before databases are affected.
 */
@Test(groups = TestGroup.UNIT)
public class SwapIndexTest extends AbstractBeanTestCase {
  private static final String NAME = "UK 3M SWAP";
  private static final String DESCRIPTION = "SWAP DESCRIPTION";
  private static final ExternalIdBundle TICKERS = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("BPSW3 Curncy"),
      ExternalSchemes.syntheticSecurityId(NAME));
  private static final Tenor TENOR = Tenor.FIVE_YEARS;
  private static final ExternalId CONVENTION_ID = ExternalId.of("SCHEME", "GBP SWAP CONVENTION");
  private static final SwapIndex INDEX_NO_DESCRIPTION = new SwapIndex(NAME, TENOR, CONVENTION_ID);
  private static final SwapIndex INDEX_WITH_DESCRIPTION = new SwapIndex(NAME, DESCRIPTION, TENOR, CONVENTION_ID);

  static {
    INDEX_NO_DESCRIPTION.setExternalIdBundle(TICKERS);
    INDEX_WITH_DESCRIPTION.setExternalIdBundle(TICKERS);
  }

  /**
   * Tests that the tenor cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTenor() {
    new SwapIndex(NAME, DESCRIPTION, null, CONVENTION_ID);
  }

  /**
   * Tests that the convention id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionId() {
    new SwapIndex(NAME, DESCRIPTION, TENOR, null);
  }

  /**
   * Tests the number of fields in the index. Will pick up additions / removals.
   */
  @Test
  public void testNumberOfFields() {
    List<Field> fields = IndexTestUtils.getFields(INDEX_NO_DESCRIPTION.getClass());
    assertEquals(15, fields.size());
    fields = IndexTestUtils.getFields(INDEX_WITH_DESCRIPTION.getClass());
    assertEquals(15, fields.size());
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
    return new JodaBeanProperties<>(SwapIndex.class, Arrays.asList("securityType", "name", "description", "tenor", "conventionId"),
        Arrays.asList(SwapIndex.INDEX_TYPE, NAME, DESCRIPTION, TENOR, CONVENTION_ID),
        Arrays.asList(BondIndex.INDEX_TYPE, "other", "INDEX DESCRIPTION", Tenor.THREE_YEARS, ExternalId.of("eid", "3")));
  }

}
