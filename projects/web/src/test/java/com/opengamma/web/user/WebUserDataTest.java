/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.user;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.master.user.ManageableUser;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;
import com.opengamma.web.MockUriInfo;

/**
 * Tests for {@link WebUserData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebUserDataTest extends AbstractBeanTestCase {
  private static final String NAME = "user";
  private static final String USER_URI = "user";
  private static final ManageableUser USER = new ManageableUser(NAME);
  private static final WebUserData DATA = new WebUserData();
  static {
    DATA.setUriInfo(new MockUriInfo(true));
    DATA.setUriUserName(USER_URI);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebUserData.class,
        Arrays.asList("uriUserName", "user"),
        Arrays.asList(USER_URI, USER),
        Arrays.asList("other", new ManageableUser("other")));
  }

  /**
   * Tests getting the best user if the override id is not null.
   */
  public void testBestUserOverrideId() {
    final String name = "best name";
    assertEquals(DATA.getBestUserUriName(name), name);
  }

  /**
   * Tests getting the best user name if there is no user.
   */
  public void testBestUserNoUser() {
    final WebUserData data = DATA.clone();
    data.setUser(null);
    assertEquals(data.getBestUserUriName(null), USER_URI);
  }

  /**
   * Tests getting the best name from the user.
   */
  public void testBestUserFrom() {
    assertEquals(DATA.getBestUserUriName(null), USER.getUserName());
  }

  /**
   * Fudge does not deserialize the user correctly.
   */
  @Override
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testCycle(final JodaBeanProperties<TYPE> properties) {
    final TYPE data = constructAndPopulateBeanBuilder(properties).build();
    assertEquals(data, cycleObjectJodaXml(properties.getType(), data));
  }

}
