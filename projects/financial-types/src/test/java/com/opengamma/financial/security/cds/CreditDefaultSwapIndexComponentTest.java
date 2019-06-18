/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.cds;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link CreditDefaultSwapIndexComponent}.
 */
@Test(groups = TestGroup.UNIT)
public class CreditDefaultSwapIndexComponentTest extends AbstractBeanTestCase {
  private static final ExternalId RED_CODE = ExternalSchemes.markItRedCode("12346");
  private static final Double WEIGHT = 0.25;
  private static final ExternalId BOND_ID = ExternalId.of("eid", "1");
  private static final String NAME = "name";

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(CreditDefaultSwapIndexComponent.class, Arrays.asList("obligorRedCode", "weight", "bondId", "name"),
        Arrays.asList(RED_CODE, WEIGHT, BOND_ID, NAME), Arrays.asList(BOND_ID, WEIGHT * 2, RED_CODE, "other"));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  public void testConstructor() {
    CreditDefaultSwapIndexComponent component = new CreditDefaultSwapIndexComponent();
    assertNull(component.getBondId());
    assertNull(component.getName());
    assertNull(component.getObligorRedCode());
    assertNull(component.getWeight());
    component = new CreditDefaultSwapIndexComponent(NAME, RED_CODE, WEIGHT, BOND_ID);
    assertEquals(component.getBondId(), BOND_ID);
    assertEquals(component.getName(), NAME);
    assertEquals(component.getObligorRedCode(), RED_CODE);
    assertEquals(component.getWeight(), WEIGHT);
  }

  /**
   * Tests the compareTo() method.
   */
  public void testCompareTo() {
    final CreditDefaultSwapIndexComponent component = new CreditDefaultSwapIndexComponent(NAME, RED_CODE, WEIGHT, BOND_ID);
    assertEquals(component.compareTo(component), 0);
    component.setBondId(RED_CODE);
    assertEquals(component.compareTo(component), 0);
    component.setName("other");
    assertEquals(component.compareTo(component), 0);
    component.setObligorRedCode(BOND_ID);
    assertEquals(component.compareTo(component), 0);
    final CreditDefaultSwapIndexComponent other = new CreditDefaultSwapIndexComponent(NAME, RED_CODE, WEIGHT * 2, BOND_ID);
    assertTrue(component.compareTo(other) < 0);
    other.setWeight(WEIGHT * 0.1);
    assertTrue(component.compareTo(other) > 0);
  }
}
