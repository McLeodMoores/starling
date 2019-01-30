/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.index;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link IndexFamily}.
 */
@Test(groups = TestGroup.UNIT)
public class IndexFamilyTest extends AbstractBeanTestCase {
  private static final String NAME = "FAMILY";
  private static final SortedMap<Tenor, ExternalId> MEMBERS = new TreeMap<>();
  private static final IndexFamily FAMILY = new IndexFamily(NAME);
  static {
    MEMBERS.put(Tenor.ONE_MONTH, ExternalId.of("sec", "1M LIBOR"));
    MEMBERS.put(Tenor.THREE_MONTHS, ExternalId.of("sec", "3M LIBOR"));
    MEMBERS.put(Tenor.SIX_MONTHS, ExternalId.of("sec", "6M LIBOR"));
    FAMILY.setMembers(MEMBERS);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(IndexFamily.class, Arrays.asList("name", "members"), Arrays.asList(NAME, MEMBERS),
        Arrays.asList("other", Collections.emptySortedMap()));
  }

  /**
   * Test that fields are set in the constructor.
   */
  public void testConstructor() {
    IndexFamily family = new IndexFamily();
    assertEquals(family.getName(), "");
    assertEquals(family.getSecurityType(), IndexFamily.METADATA_TYPE);
    assertTrue(family.getMembers().isEmpty());
    family = new IndexFamily(NAME);
    assertEquals(family.getName(), NAME);
    assertEquals(family.getSecurityType(), IndexFamily.METADATA_TYPE);
    assertTrue(family.getMembers().isEmpty());
  }

}
