/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.security.auditlog;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Date;

import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link AuditLogEntry}.
 */
@Test(groups = TestGroup.UNIT)
public class AuditLogEntryTest extends AbstractFudgeBuilderTestCase {
  private static final Long ID = 123L;
  private static final String USER = "user";
  private static final String ORIGINATING_SYSTEM = "system";
  private static final String OBJECT = "object";
  private static final String OPERATION = "operation";
  private static final String DESCRIPTION = "description";
  private static final boolean SUCCESS = true;
  private static final Date TIME_STAMP = new Date(223489795L);

  /**
   * Tests that the user cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUser() {
    new AuditLogEntry(null, ORIGINATING_SYSTEM, OBJECT, OPERATION, DESCRIPTION, SUCCESS, TIME_STAMP);
  }

  /**
   * Tests that the originating system cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOriginatingSystem() {
    new AuditLogEntry(USER, null, OBJECT, OPERATION, DESCRIPTION, SUCCESS, TIME_STAMP);
  }

  /**
   * Tests that the object cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObject() {
    new AuditLogEntry(USER, ORIGINATING_SYSTEM, null, OPERATION, DESCRIPTION, SUCCESS, TIME_STAMP);
  }

  /**
   * Tests that the operation cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOperation() {
    new AuditLogEntry(USER, ORIGINATING_SYSTEM, OBJECT, null, DESCRIPTION, SUCCESS, TIME_STAMP);
  }

  /**
   * Tests that the timestamp cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimeStamp() {
    new AuditLogEntry(USER, ORIGINATING_SYSTEM, OBJECT, OPERATION, DESCRIPTION, SUCCESS, null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final AuditLogEntry entry = new AuditLogEntry(USER, ORIGINATING_SYSTEM, OBJECT, OPERATION, DESCRIPTION, SUCCESS, TIME_STAMP);
    entry.setId(ID);
    assertEquals(entry.getDescription(), DESCRIPTION);
    assertEquals(entry.getId(), ID);
    assertEquals(entry.getObject(), OBJECT);
    assertEquals(entry.getOperation(), OPERATION);
    assertEquals(entry.getOriginatingSystem(), ORIGINATING_SYSTEM);
    assertEquals(entry.getTimestamp(), TIME_STAMP);
    assertEquals(entry.getUser(), USER);
    assertEquals(entry, entry);
    assertNotEquals(null, entry);
    assertNotEquals(USER, entry);
    assertEquals(entry.toString(), "AuditLogEntry[_id=123,_user=user,_originatingSystem=system,"
        + "_object=object,_operation=operation,_description=description,_success=true,"
        + "_timestamp=Sat Jan 03 15:04:49 GMT 1970]");
    final AuditLogEntry other = new AuditLogEntry();
    assertNotEquals(other, entry);
    other.setId(null);
    assertNotEquals(other, entry);
    other.setId(ID);
    assertEquals(other, entry);
    other.setDescription(USER);
    assertEquals(other, entry);
    other.setObject(USER);
    assertEquals(other, entry);
    other.setOriginatingSystem(USER);
    assertEquals(other, entry);
    other.setOperation(USER);
    assertEquals(other, entry);
    other.setTimestamp(new Date(4837932874L));
    assertEquals(other, entry);
    other.setUser(DESCRIPTION);
    assertEquals(other, entry);
    other.setSuccess(false);
    assertEquals(other, entry);
    entry.setId(null);
    other.setId(ID);
    assertNotEquals(other, entry);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    AuditLogEntry entry = new AuditLogEntry(USER, ORIGINATING_SYSTEM, OBJECT, OPERATION, DESCRIPTION, SUCCESS, TIME_STAMP);
    entry.setId(ID);
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    assertEquals(AuditLogEntry.fromFudgeMsg(entry.toFudgeMsg(serializer)),
        new AuditLogEntry(USER, ORIGINATING_SYSTEM, OBJECT, OPERATION, DESCRIPTION, SUCCESS, TIME_STAMP));
    entry = new AuditLogEntry(USER, ORIGINATING_SYSTEM, OBJECT, OPERATION, null, SUCCESS, TIME_STAMP);
    assertEncodeDecodeCycle(AuditLogEntry.class, entry);
  }
}
