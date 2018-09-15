/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.security.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link Authority}.
 */
@Test(groups = TestGroup.UNIT)
public class AuthorityTest {
  private static final Long ID = 100L;
  private static final String REGEX = "*/";

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final Authority authority = new Authority(ID, REGEX);
    assertEquals(authority, authority);
    assertNotEquals(null, authority);
    assertNotEquals(REGEX, authority);
    assertEquals(authority.getId(), ID);
    assertEquals(authority.getRegex(), REGEX);
    assertEquals(authority.toString(), REGEX);
    Authority other = new Authority(REGEX);
    assertNull(other.getId());
    other = new Authority();
    other.setId(ID);
    other.setRegex(REGEX);
    assertEquals(authority, other);
    assertEquals(authority.hashCode(), other.hashCode());
    other = new Authority(ID + 1, REGEX);
    assertNotEquals(authority, other);
    other = new Authority(ID, "*/*/");
    assertEquals(authority, other);
  }

  /**
   * Tests the matcher.
   */
  @Test
  public void testMatcher() {
    final String s = "foo/bar";
    assertEquals(new Authority(ID, REGEX).matches(s), PathMatcher.matches(s, REGEX));
  }
}
