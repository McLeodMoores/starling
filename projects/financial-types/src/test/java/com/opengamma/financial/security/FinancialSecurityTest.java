/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FinancialSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class FinancialSecurityTest extends AbstractBeanTestCase {
  private static final UniqueId UID = UniqueId.of("sec", "1");
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("eid", "10");
  private static final String NAME = "name";
  private static final String TYPE = "DUMMY";
  private static final Map<String, String> ATTRIBUTES = Collections.singletonMap("attr", "100");
  private static final Set<String> PERMISSIONS = Collections.singleton("perm");

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(DummySecurity.class,
        Arrays.asList("uniqueId", "externalIdBundle", "name", "securityType", "attributes", "requiredPermissions"),
        Arrays.asList(UID, EIDS, NAME, TYPE, ATTRIBUTES, PERMISSIONS), Arrays.asList(UniqueId.of("sec", "2"), ExternalIdBundle.of("eid", "200"), TYPE, NAME,
            Collections.singletonMap("attr", "200"), Collections.singleton("req")));
  }

  /**
   * Tests that the fields are set correctly.
   */
  @SuppressWarnings("deprecation")
  public void testConstructor() {
    DummySecurity security = new DummySecurity(TYPE);
    assertTrue(security.getAttributes().isEmpty());
    assertTrue(security.getExternalIdBundle().isEmpty());
    assertTrue(security.getName().isEmpty());
    assertTrue(security.getPermissions().isEmpty());
    assertTrue(security.getRequiredPermissions().isEmpty());
    assertEquals(security.getSecurityType(), TYPE);
    assertNull(security.getUniqueId());
    security = new DummySecurity(UID, NAME, TYPE, EIDS);
    assertTrue(security.getAttributes().isEmpty());
    assertEquals(security.getExternalIdBundle(), EIDS);
    assertEquals(security.getName(), NAME);
    assertTrue(security.getPermissions().isEmpty());
    assertTrue(security.getRequiredPermissions().isEmpty());
    assertEquals(security.getSecurityType(), TYPE);
    assertEquals(security.getUniqueId(), UID);
  }

  /**
   * Tests a cycle.
   */

}
