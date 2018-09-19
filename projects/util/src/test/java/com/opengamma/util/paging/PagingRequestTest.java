/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test PagingRequest.
 */
@Test(groups = TestGroup.UNIT)
public final class PagingRequestTest {

  public void testAll() {
    final PagingRequest test = PagingRequest.ALL;
    assertEquals(0, test.getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPagingSize());
  }

  public void testFirstPage() {
    final PagingRequest test = PagingRequest.FIRST_PAGE;
    assertEquals(0, test.getFirstItem());
    assertEquals(20, test.getPagingSize());
  }

  public void testOne() {
    final PagingRequest test = PagingRequest.ONE;
    assertEquals(0, test.getFirstItem());
    assertEquals(1, test.getPagingSize());
  }

  public void testNone() {
    final PagingRequest test = PagingRequest.NONE;
    assertEquals(0, test.getFirstItem());
    assertEquals(0, test.getPagingSize());
  }

  //-------------------------------------------------------------------------
  public void testOfPageDefaulted() {
    assertEquals(0, PagingRequest.ofPageDefaulted(1, 10).getFirstItem());
    assertEquals(10, PagingRequest.ofPageDefaulted(1, 10).getPagingSize());

    assertEquals(0, PagingRequest.ofPageDefaulted(0, 10).getFirstItem());
    assertEquals(10, PagingRequest.ofPageDefaulted(0, 10).getPagingSize());

    assertEquals(20, PagingRequest.ofPageDefaulted(2, 0).getFirstItem());
    assertEquals(20, PagingRequest.ofPageDefaulted(2, 0).getPagingSize());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfPageDefaulted2IntsPageNegative() {
    PagingRequest.ofPageDefaulted(-1, 40);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfPageDefaulted2IntsPagingSizeNegative() {
    PagingRequest.ofPageDefaulted(1, -1);
  }

  //-------------------------------------------------------------------------
  public void testOfPage() {
    assertEquals(0, PagingRequest.ofPage(1, 10).getFirstItem());
    assertEquals(10, PagingRequest.ofPage(1, 10).getPagingSize());

    assertEquals(0, PagingRequest.ofPage(2, 0).getFirstItem());
    assertEquals(0, PagingRequest.ofPage(2, 0).getPagingSize());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfPage2IntsPage0() {
    PagingRequest.ofPage(0, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfPage2IntsPageNegative() {
    PagingRequest.ofPage(-1, 40);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ofPage_2ints_pagingSizeNegative() {
    PagingRequest.ofPage(1, -1);
  }

  //-------------------------------------------------------------------------
  public void test_getItems_index() {
    final PagingRequest test = PagingRequest.ofIndex(5, 25);
    assertEquals(5, test.getFirstItem());
    assertEquals(30, test.getLastItem());
    assertEquals(6, test.getFirstItemOneBased());
    assertEquals(30, test.getLastItemOneBased());
  }

  public void test_getItems_page1() {
    final PagingRequest test = PagingRequest.ofPage(1, 20);
    assertEquals(0, test.getFirstItem());
    assertEquals(20, test.getLastItem());
    assertEquals(1, test.getFirstItemOneBased());
    assertEquals(20, test.getLastItemOneBased());
  }

  public void test_getItems_page2() {
    final PagingRequest test = PagingRequest.ofPage(2, 20);
    assertEquals(20, test.getFirstItem());
    assertEquals(40, test.getLastItem());
    assertEquals(21, test.getFirstItemOneBased());
    assertEquals(40, test.getLastItemOneBased());
  }

  //-------------------------------------------------------------------------
  public void test_select_firstPage() {
    final PagingRequest test = PagingRequest.ofPage(1, 2);
    final List<String> coll = Arrays.asList("Hello", "World", "Test");
    final List<String> result = test.select(coll);
    assertEquals(Arrays.asList("Hello", "World"), result);
  }

  public void test_select_lastPage() {
    final PagingRequest test = PagingRequest.ofPage(2, 2);
    final List<String> coll = Arrays.asList("Hello", "World", "Test");
    final List<String> result = test.select(coll);
    assertEquals(Arrays.asList("Test"), result);
  }

  public void test_select_all() {
    final PagingRequest test = PagingRequest.ofPage(1, 20);
    final List<String> coll = Arrays.asList("Hello", "World", "Test");
    final List<String> result = test.select(coll);
    assertEquals(coll, result);
  }

  public void test_select_disconnected() {
    final PagingRequest test = PagingRequest.ofPage(1, 2);
    final List<String> coll = Arrays.asList("Hello", "World", "Test");
    final List<String> result = test.select(coll);
    result.set(0, "Changed");
    assertEquals(Arrays.asList("Changed", "World"), result);
    assertEquals(Arrays.asList("Hello", "World", "Test"), coll);
  }

  public void test_select_beyondListSize() {
    final PagingRequest test = PagingRequest.ofPage(3, 2);
    final List<String> coll = Arrays.asList("Hello", "World", "Test");
    final List<String> result = test.select(coll);
    assertEquals(0, result.size());
    assertEquals(Arrays.asList("Hello", "World", "Test"), coll);
  }

  //-------------------------------------------------------------------------
  public void test_equals_equal() {
    final PagingRequest test1 = PagingRequest.ofPage(1, 20);
    final PagingRequest test2 = PagingRequest.ofPage(1, 20);
    assertEquals(true, test1.equals(test1));
    assertEquals(true, test1.equals(test2));
    assertEquals(true, test2.equals(test1));
    assertEquals(true, test2.equals(test2));
  }

  public void test_equals_notEqualPage() {
    final PagingRequest test1 = PagingRequest.ofPage(1, 20);
    final PagingRequest test2 = PagingRequest.ofPage(2, 20);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  public void test_equals_notEqualPagingSize() {
    final PagingRequest test1 = PagingRequest.ofPage(1, 20);
    final PagingRequest test2 = PagingRequest.ofPage(1, 30);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  public void test_equals_other() {
    final PagingRequest test = PagingRequest.ofPage(1, 20);
    assertEquals(false, test.equals(""));
    assertEquals(false, test.equals(null));
  }

  //-------------------------------------------------------------------------
  public void test_hashCode_equal() {
    final PagingRequest test1 = PagingRequest.ofPage(2, 40);
    final PagingRequest test2 = PagingRequest.ofPage(2, 40);
    assertEquals(test1.hashCode(), test2.hashCode());
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    final PagingRequest test = PagingRequest.ofIndex(3, 40);
    assertEquals("PagingRequest[first=3, size=40]", test.toString());
  }

}
