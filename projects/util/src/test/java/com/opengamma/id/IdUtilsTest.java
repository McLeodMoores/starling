/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link IdUtils}.
 */
@Test(groups = TestGroup.UNIT)
public class IdUtilsTest {

  /**
   * Tests the private constructor via reflection.
   *
   * @throws Exception  if the class cannot be created
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testConstructor() throws Exception {
    final Constructor<?>[] cons = IdUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    final Constructor<IdUtils> con = (Constructor<IdUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the unique id can be set if the object is MutableUniqueIdentifiable.
   */
  @Test
  public void testSetSuccess() {
    final UniqueId uniqueId = UniqueId.of("A", "B");
    final MockMutable mock = new MockMutable();
    IdUtils.setInto(mock, uniqueId);
    assertEquals(uniqueId, mock.getUniqueId());
  }

  /**
   * Checks that no error is thrown if the object is not MutableUniqueIdentifiable.
   */
  @Test
  public void testSetNotMutableUniqueIdentifiable() {
    final UniqueId uniqueId = UniqueId.of("A", "B");
    IdUtils.setInto(new Object(), uniqueId);
    // no error
  }

  /**
   * Test class.
   */
  static class MockMutable implements MutableUniqueIdentifiable {
    private UniqueId _uniqueId;

    @Override
    public void setUniqueId(final UniqueId uniqueId) {
      _uniqueId = uniqueId;
    }

    /**
     * Gets the id.
     * @return  the id
     */
    public UniqueId getUniqueId() {
      return _uniqueId;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the conversion of ObjectIds and UniqueIds to a string list.
   */
  @Test
  public void testToStringList() {
    final Iterable<ObjectIdentifiable> objectIds =
        ImmutableList.<ObjectIdentifiable>of(ObjectId.of("A", "X"), UniqueId.of("B", "Y", "1"), ObjectId.of("C", "Z"));
    final Iterable<String> expected = ImmutableList.of("A~X", "B~Y~1", "C~Z");
    final List<String> test = IdUtils.toStringList(objectIds);
    assertEquals(expected, test);
  }

  /**
   * Tests the conversion of null to a string list.
   */
  @Test
  public void testToStringListNull() {
    final List<String> test = IdUtils.toStringList(null);
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests parsing object ids from a lit of strings.
   */
  @Test
  public void testParseObjectIds() {
    final Iterable<String> objectIds = ImmutableList.of("A~X", "B~Y", "C~Z");
    final Iterable<ObjectId> expected = ImmutableList.of(ObjectId.of("A", "X"), ObjectId.of("B", "Y"), ObjectId.of("C", "Z"));
    final List<ObjectId> test = IdUtils.parseObjectIds(objectIds);
    assertEquals(expected, test);
  }

  /**
   * Tests that attempting to parse a null does nothing.
   */
  @Test
  public void testParseObjectIdsNull() {
    final List<ObjectId> test = IdUtils.parseObjectIds(null);
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests parsing unique ids from a list of strings.
   */
  @Test
  public void testParseUniqueIds() {
    final Iterable<String> objectIds = ImmutableList.of("A~X", "B~Y~1", "C~Z");
    final Iterable<UniqueId> expected = ImmutableList.of(UniqueId.of("A", "X"), UniqueId.of("B", "Y", "1"), UniqueId.of("C", "Z"));
    final List<UniqueId> test = IdUtils.parseUniqueIds(objectIds);
    assertEquals(expected, test);
  }

  /**
   * Tests that attempting to parse a null does nothing.
   */
  @Test
  public void testParseUniqueIdsNull() {
    final List<UniqueId> test = IdUtils.parseUniqueIds(null);
    assertEquals(0, test.size());
  }

}
