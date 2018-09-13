/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ManageableUnstructuredMarketDataSnapshot} class.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableUnstructuredMarketDataSnapshotTest extends AbstractFudgeBuilderTestCase {
  private static final ExternalId EID_1 = ExternalId.of("Foo", "1");
  private static final ExternalId EID_2 = ExternalId.of("Foo", "2");
  private static final ExternalId EID_3 = ExternalId.of("Foo", "3");
  private static final ManageableUnstructuredMarketDataSnapshot SNAPSHOT = new ManageableUnstructuredMarketDataSnapshot();
  static {
    SNAPSHOT.putValue(EID_1, "V1", ValueSnapshot.of(11d));
    SNAPSHOT.putValue(EID_1, "V2", ValueSnapshot.of(12d));
    SNAPSHOT.putValue(EID_2, "V1", ValueSnapshot.of(21d));
    SNAPSHOT.putValue(EID_2, "V2", ValueSnapshot.of(22d));
    SNAPSHOT.putValue(EID_3, "V1", ValueSnapshot.of(31d));
    SNAPSHOT.putValue(EID_3, "V2", ValueSnapshot.of(32d));
  }

  /**
   * Tests adding and removing values by external id.
   */
  public void testPutGetRemoveExternalId() {
    final ManageableUnstructuredMarketDataSnapshot object = new ManageableUnstructuredMarketDataSnapshot();
    assertTrue(object.isEmpty());
    object.putValue(EID_1, "V1", ValueSnapshot.of(11d));
    object.putValue(EID_1, "V2", ValueSnapshot.of(12d));
    object.putValue(EID_2, "V1", ValueSnapshot.of(21d));
    object.putValue(EID_2, "V2", ValueSnapshot.of(22d));
    assertFalse(object.isEmpty());
    assertEquals(object.getTargets(), ImmutableSet.of(EID_1.toBundle(), EID_2.toBundle()));
    assertEquals(object.getValue(EID_1, "V1"), ValueSnapshot.of(11d));
    assertEquals(object.getValue(EID_1, "V2"), ValueSnapshot.of(12d));
    assertNull(object.getValue(EID_1, "V3"));
    assertEquals(object.getValue(EID_2, "V1"), ValueSnapshot.of(21d));
    assertEquals(object.getValue(EID_2, "V2"), ValueSnapshot.of(22d));
    assertNull(object.getValue(EID_2, "V3"));
    assertNull(object.getValue(EID_3, "V1"));
    assertNull(object.getValue(EID_3, "V2"));
    assertEquals(object.getValue(ExternalIdBundle.of(EID_1, EID_3), "V1"), ValueSnapshot.of(11d));
    final ManageableUnstructuredMarketDataSnapshot cloned = new ManageableUnstructuredMarketDataSnapshot(object);
    object.removeValue(EID_1, "V1");
    object.removeValue(EID_2, "V1");
    assertEquals(object.getTargets(), ImmutableSet.of(EID_1.toBundle(), EID_2.toBundle()));
    assertNull(object.getValue(EID_1, "V1"));
    assertEquals(object.getValue(EID_1, "V2"), ValueSnapshot.of(12d));
    assertNull(object.getValue(EID_2, "V1"));
    assertEquals(object.getValue(EID_2, "V2"), ValueSnapshot.of(22d));
    object.removeValue(EID_1, "V2");
    assertEquals(object.getTargets(), ImmutableSet.of(EID_2.toBundle()));
    object.removeValue(EID_2, "V2");
    assertEquals(object.getTargets(), Collections.emptySet());
    assertTrue(object.isEmpty());
    assertEquals(cloned.getValue(EID_1, "V1"), ValueSnapshot.of(11d));
    assertEquals(cloned.getValue(EID_1, "V2"), ValueSnapshot.of(12d));
    assertEquals(cloned.getValue(EID_2, "V1"), ValueSnapshot.of(21d));
    assertEquals(cloned.getValue(EID_2, "V2"), ValueSnapshot.of(22d));
  }

  /**
   * Tests adding and removing values by external id bundle.
   */
  public void testPutGetRemoveExternalIdBundle() {
    final ManageableUnstructuredMarketDataSnapshot object = new ManageableUnstructuredMarketDataSnapshot();
    assertTrue(object.isEmpty());
    object.putValue(ExternalIdBundle.of(EID_1, EID_2), "V1", ValueSnapshot.of(1d));
    object.putValue(ExternalIdBundle.of(EID_2, EID_3), "V2", ValueSnapshot.of(2d));
    assertEquals(object.getTargets(), ImmutableSet.of(ExternalIdBundle.of(EID_1, EID_2), ExternalIdBundle.of(EID_2, EID_3)));
    assertFalse(object.isEmpty());
    assertEquals(object.getValue(ExternalIdBundle.of(EID_1, EID_2), "V1"), ValueSnapshot.of(1d));
    assertEquals(object.getValue(ExternalIdBundle.of(EID_1, EID_2), "V2"), ValueSnapshot.of(2d));
    assertEquals(object.getValue(EID_1, "V1"), ValueSnapshot.of(1d));
    assertNull(object.getValue(EID_1, "V2"));
    assertEquals(object.getValue(EID_2, "V1"), ValueSnapshot.of(1d));
    assertEquals(object.getValue(EID_2, "V2"), ValueSnapshot.of(2d));
    object.putValue(ExternalIdBundle.of(EID_2, EID_3), "V1", ValueSnapshot.of(3d));
    assertEquals(object.getTargets(), ImmutableSet.of(ExternalIdBundle.of(EID_2, EID_3)));
    assertNull(object.getValue(EID_1, "V1"));
    assertNull(object.getValue(EID_1, "V2"));
    assertEquals(object.getValue(EID_2, "V1"), ValueSnapshot.of(3d));
    assertEquals(object.getValue(EID_2, "V2"), ValueSnapshot.of(2d));
    assertEquals(object.getValue(EID_3, "V1"), ValueSnapshot.of(3d));
    assertEquals(object.getValue(EID_3, "V2"), ValueSnapshot.of(2d));
    final ManageableUnstructuredMarketDataSnapshot cloned = new ManageableUnstructuredMarketDataSnapshot(object);
    object.removeValue(ExternalIdBundle.of(EID_2, EID_3), "V1");
    assertEquals(object.getTargets(), ImmutableSet.of(ExternalIdBundle.of(EID_2, EID_3)));
    assertNull(object.getValue(EID_2, "V1"));
    assertEquals(object.getValue(EID_2, "V2"), ValueSnapshot.of(2d));
    assertNull(object.getValue(EID_3, "V1"));
    assertEquals(object.getValue(EID_3, "V2"), ValueSnapshot.of(2d));
    object.removeValue(ExternalIdBundle.of(EID_1, EID_2), "V2");
    assertEquals(object.getTargets(), Collections.emptySet());
    assertTrue(object.isEmpty());
    assertEquals(cloned.getValue(EID_2, "V1"), ValueSnapshot.of(3d));
    assertEquals(cloned.getValue(EID_2, "V2"), ValueSnapshot.of(2d));
    assertEquals(cloned.getValue(EID_3, "V1"), ValueSnapshot.of(3d));
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(ManageableUnstructuredMarketDataSnapshot.class, SNAPSHOT);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(SNAPSHOT, SNAPSHOT);
    assertNotEquals(null, SNAPSHOT);
    assertNotEquals("", SNAPSHOT);
    ManageableUnstructuredMarketDataSnapshot other = SNAPSHOT.clone();
    assertEquals(SNAPSHOT, other);
    assertEquals(SNAPSHOT.hashCode(), other.hashCode());
    other = new ManageableUnstructuredMarketDataSnapshot();
    assertNotEquals(SNAPSHOT, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(SNAPSHOT.metaBean());
    assertNotNull(SNAPSHOT.metaBean().values());
    assertEquals(SNAPSHOT.metaBean().values().get(SNAPSHOT), SNAPSHOT.property("values").get());
  }
}
