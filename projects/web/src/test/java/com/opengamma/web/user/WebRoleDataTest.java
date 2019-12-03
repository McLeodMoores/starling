/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.master.user.ManageableRole;
import com.opengamma.master.user.impl.InMemoryUserMaster;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;
import com.opengamma.web.MockUriInfo;

/**
 * Tests for {@link WebRoleData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebRoleDataTest extends AbstractBeanTestCase {
  private static final String NAME = "role";
  private static final String ROLE_URI = "roleUri";
  private static final ManageableRole ROLE = new ManageableRole(NAME);
  private static final WebRoleData DATA = new WebRoleData();
  static {
    DATA.setUriInfo(new MockUriInfo(true));
    DATA.setUriRoleName(ROLE_URI);
    DATA.setRole(ROLE);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebRoleData.class, Arrays.asList("uriRoleName", "role"), Arrays.asList(ROLE_URI, ROLE),
        Arrays.asList("other", new ManageableRole("other")));
  }

  /**
   * Tests getting the role master if the user master is not set.
   */
  public void testGetRoleMasterNoUserMaster() {
    assertNull(DATA.getRoleMaster());
  }

  /**
   * Tests getting the role master.
   */
  public void testGetRoleMaster() {
    final InMemoryUserMaster userMaster = new InMemoryUserMaster();
    final WebRoleData data = DATA.clone();
    data.setUserMaster(userMaster);
    assertNotNull(data.getRoleMaster());
  }

  /**
   * Tests getting the best role if the override id is not null.
   */
  public void testBestUserOverrideId() {
    final String name = "best name";
    assertEquals(DATA.getBestRoleUriName(name), name);
  }

  /**
   * Tests getting the best role name if there is no user.
   */
  public void testBestUserNoUser() {
    final WebRoleData data = DATA.clone();
    data.setRole(null);
    assertEquals(data.getBestRoleUriName(null), ROLE_URI);
  }

  /**
   * Tests getting the best name from the role.
   */
  public void testBestUserFromRole() {
    assertEquals(DATA.getBestRoleUriName(null), ROLE.getRoleName());
  }

}
