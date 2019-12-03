/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RegexUtilsTest {

  /**
   * @throws Exception
   *           if there is a problem
   */
  @SuppressWarnings("unchecked")
  public void testConstructor() throws Exception {
    final Constructor<?>[] cons = RegexUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    final Constructor<RegexUtils> con = (Constructor<RegexUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    assertEquals(null, RegexUtils.wildcardsToPattern(null));
  }

  /**
   *
   */
  public void testStar() {
    assertEquals(Pattern.compile("^\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("Hello*").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E$").toString(), RegexUtils.wildcardsToPattern("*Hello").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("*Hello*").toString());
    assertEquals(Pattern.compile("^\\QHe\\E.*\\Qllo\\E$").toString(), RegexUtils.wildcardsToPattern("He*llo").toString());
  }

  /**
   *
   */
  public void testDoubleStar() {
    assertEquals(Pattern.compile("^\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("Hello**").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E$").toString(), RegexUtils.wildcardsToPattern("**Hello").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("**Hello*").toString());
    assertEquals(Pattern.compile("^\\QHe\\E.*\\Qllo\\E$").toString(), RegexUtils.wildcardsToPattern("He**llo").toString());
  }

  /**
   *
   */
  public void testQuestion() {
    assertEquals(Pattern.compile("^\\QHello\\E.$").toString(), RegexUtils.wildcardsToPattern("Hello?").toString());
    assertEquals(Pattern.compile("^.\\QHello\\E$").toString(), RegexUtils.wildcardsToPattern("?Hello").toString());
    assertEquals(Pattern.compile("^.\\QHello\\E.$").toString(), RegexUtils.wildcardsToPattern("?Hello?").toString());
    assertEquals(Pattern.compile("^\\QHe\\E.\\Qllo\\E$").toString(), RegexUtils.wildcardsToPattern("He?llo").toString());
  }

  /**
   *
   */
  public void testEscape() {
    assertEquals(Pattern.compile("^\\QH\\E.*\\Qel[l\\E.\\Qo\\E$").toString(), RegexUtils.wildcardsToPattern("H*el[l?o").toString());
  }

  /**
   *
   */
  public void testMatchesStar() {
    assertEquals(true, RegexUtils.wildcardMatch("Hello*", "Hello"));
    assertEquals(true, RegexUtils.wildcardMatch("Hello*", "Hello world"));
    assertEquals(false, RegexUtils.wildcardMatch("Hello*", "Hell on earth"));
    assertEquals(false, RegexUtils.wildcardMatch(null, "Hell on earth"));
    assertEquals(false, RegexUtils.wildcardMatch("Hello*", null));
  }

}
