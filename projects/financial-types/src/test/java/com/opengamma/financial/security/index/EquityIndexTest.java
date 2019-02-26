/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.index;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
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
 * Tests the fields of an equity index. This test is intended to pick up any changes before databases are affected.
 */
@Test(groups = TestGroup.UNIT)
public class EquityIndexTest extends AbstractBeanTestCase {
  /** The index name */
  private static final String NAME = "EQUITY INDEX";
  /** The index description */
  private static final String DESCRIPTION = "EQUITY INDEX DESCRIPTION";
  /** The first index member */
  private static final ExternalIdBundle ID1 = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("A"));
  /** The second index member */
  private static final ExternalIdBundle ID2 = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("B"));
  /** The components */
  private static final List<EquityIndexComponent> COMPONENTS = Arrays.asList(new EquityIndexComponent(ID1, new BigDecimal(0.7)),
      new EquityIndexComponent(ID2, new BigDecimal(0.3)));
  /** The weighting type */
  private static final IndexWeightingType WEIGHTING_TYPE = IndexWeightingType.EQUAL;
  /** The index */
  private static final EquityIndex INDEX_NO_DESCRIPTION = new EquityIndex(NAME, COMPONENTS, WEIGHTING_TYPE);
  /** The index */
  private static final EquityIndex INDEX_WITH_DESCRIPTION = new EquityIndex(NAME, DESCRIPTION, COMPONENTS, WEIGHTING_TYPE);

  /**
   * Tests that the components cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullComponents() {
    new EquityIndex(NAME, DESCRIPTION, null, WEIGHTING_TYPE);
  }

  /**
   * Tests that the components cannot be empty.
   */
  @Test
  public void testEmptyComponents() {
    new EquityIndex(NAME, DESCRIPTION, new ArrayList<EquityIndexComponent>(), WEIGHTING_TYPE);
  }

  /**
   * Tests that the weighting type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeightingType() {
    new EquityIndex(NAME, DESCRIPTION, COMPONENTS, null);
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
    assertEquals(COMPONENTS, INDEX_NO_DESCRIPTION.getEquityComponents());
    assertEquals(COMPONENTS, INDEX_WITH_DESCRIPTION.getEquityComponents());
    assertEquals(WEIGHTING_TYPE, INDEX_NO_DESCRIPTION.getWeightingType());
    assertEquals(WEIGHTING_TYPE, INDEX_WITH_DESCRIPTION.getWeightingType());
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(EquityIndex.class, Arrays.asList("securityType", "description", "indexFamilyId", "equityComponents", "weightingType"),
        Arrays.asList(BondIndex.INDEX_TYPE, DESCRIPTION, ExternalId.of("eid", "1"), COMPONENTS, WEIGHTING_TYPE),
        Arrays.asList(IborIndex.INDEX_TYPE, "INDEX DESCRIPTION", ExternalId.of("eid", "2"), COMPONENTS.subList(0, 1), IndexWeightingType.PRICE));
  }

}
