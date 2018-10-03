/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ManageableCurveSnapshot}.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableCurveSnapshotTest extends AbstractFudgeBuilderTestCase {
  /** The valuation instant */
  private static final Instant INSTANT = Instant.now();
  /** The values */
  private static final ManageableUnstructuredMarketDataSnapshot VALUES;
  /** An id */
  private static final ExternalId ID1 = ExternalId.of("Test", "1");
  /** An id name */
  private static final String ID1_NAME = "ID1";
  /** A value */
  private static final ValueSnapshot ID1_VALUE = ValueSnapshot.of(100);
  /** An id */
  private static final ExternalId ID2 = ExternalId.of("Test", "2");
  /** An id name */
  private static final String ID2_NAME = "ID2";
  /** A value */
  private static final ValueSnapshot ID2_VALUE = ValueSnapshot.of(200);
  /** An id */
  private static final ExternalId ID3 = ExternalId.of("Test", "3");
  /** An id name */
  private static final String ID3_NAME = "ID3";
  /** A value */
  private static final ValueSnapshot ID3_VALUE = ValueSnapshot.of(300);
  static {
    VALUES = new ManageableUnstructuredMarketDataSnapshot();
    VALUES.putValue(ID1, ID1_NAME, ID1_VALUE);
    VALUES.putValue(ID2, ID2_NAME, ID2_VALUE);
    VALUES.putValue(ID3, ID3_NAME, ID3_VALUE);
  }
  private static final ManageableCurveSnapshot SNAPSHOT = new ManageableCurveSnapshot(INSTANT, VALUES);

  /**
   * Tests the snapshot object.
   */
  @Test
  public void testObject() {
    ManageableCurveSnapshot other = new ManageableCurveSnapshot(INSTANT, VALUES);
    assertEquals(SNAPSHOT, SNAPSHOT);
    assertEquals(SNAPSHOT, other);
    assertEquals(SNAPSHOT.hashCode(), other.hashCode());
    other = ManageableCurveSnapshot.of(INSTANT, VALUES);
    assertEquals(SNAPSHOT, other);
    other = new ManageableCurveSnapshot(INSTANT.plusMillis(100), VALUES);
    assertNotEquals(SNAPSHOT, other);
    other = new ManageableCurveSnapshot(INSTANT, new ManageableUnstructuredMarketDataSnapshot());
    assertNotEquals(SNAPSHOT, other);
  }

  /**
   * Tests conversion to and from a Fudge message.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testFudgeMessage() {
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final FudgeDeserializer deserializer = new FudgeDeserializer(getFudgeContext());
    final FudgeMsg message = SNAPSHOT.toFudgeMsg(serializer);
    final ManageableCurveSnapshot cycled = ManageableCurveSnapshot.fromFudgeMsg(deserializer, message);
    assertEquals(SNAPSHOT, cycled);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(ManageableCurveSnapshot.class, SNAPSHOT);
  }
}
