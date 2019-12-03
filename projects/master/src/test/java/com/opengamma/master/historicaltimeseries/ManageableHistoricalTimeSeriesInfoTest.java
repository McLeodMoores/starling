/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo.Meta;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ManageableHistoricalTimeSeriesInfo}.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableHistoricalTimeSeriesInfoTest extends AbstractFudgeBuilderTestCase {
  private static final UniqueId UID = UniqueId.of("hist", "1");
  private static final ExternalIdBundleWithDates EIDS = ExternalIdBundleWithDates.of(ExternalIdBundle.of(ExternalId.of("A", "abc"), ExternalId.of("B", "ABC")));
  private static final String NAME = "name";
  private static final String DATA_FIELD = "field";
  private static final String DATA_SOURCE = "source";
  private static final String DATA_PROVIDER = "provider";
  private static final String OBS_TIME = "obs";
  private static final ObjectId OID = ObjectId.of("a", "abc");
  private static final Set<String> PERMISSIONS = new HashSet<>(Arrays.asList("perm1", "perm2"));

  /**
   * Tests that the required permissions cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRequiredPermissions() {
    new ManageableHistoricalTimeSeriesInfo().setRequiredPermissions(null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    assertTrue(info.getRequiredPermissions().isEmpty());
    setFields(info);
    assertEquals(info.getDataField(), DATA_FIELD);
    assertEquals(info.getDataProvider(), DATA_PROVIDER);
    assertEquals(info.getDataSource(), DATA_SOURCE);
    assertEquals(info.getExternalIdBundle(), EIDS);
    assertEquals(info.getName(), NAME);
    assertEquals(info.getObservationTime(), OBS_TIME);
    assertEquals(info.getRequiredPermissions(), PERMISSIONS);
    assertEquals(info.getTimeSeriesObjectId(), OID);
    assertEquals(info.getUniqueId(), UID);
    assertEquals(info, info);
    assertEquals(info.toString(),
        "ManageableHistoricalTimeSeriesInfo{uniqueId=hist~1, externalIdBundle=BundleWithDates[A~abc, B~ABC], "
            + "name=name, dataField=field, dataSource=source, dataProvider=provider, observationTime=obs, timeSeriesObjectId=a~abc, "
            + "requiredPermissions=[perm1, perm2]}");
    final ManageableHistoricalTimeSeriesInfo other = new ManageableHistoricalTimeSeriesInfo();
    setFields(other);
    assertEquals(info, other);
    assertEquals(info.hashCode(), other.hashCode());
    other.setDataField(DATA_PROVIDER);
    assertNotEquals(info, other);
    other.setDataField(null);
    assertNotEquals(info, other);
    setFields(other);
    other.setDataProvider(DATA_FIELD);
    assertNotEquals(info, other);
    other.setDataProvider(null);
    assertNotEquals(info, other);
    setFields(other);
    other.setDataProvider(DATA_FIELD);
    assertNotEquals(info, other);
    other.setDataProvider(null);
    assertNotEquals(info, other);
    setFields(other);
    other.setExternalIdBundle(ExternalIdBundleWithDates.EMPTY);
    assertNotEquals(info, other);
    other.setExternalIdBundle(null);
    assertNotEquals(info, other);
    setFields(other);
    other.setName(DATA_PROVIDER);
    assertNotEquals(info, other);
    other.setName(null);
    assertNotEquals(info, other);
    setFields(other);
    other.setObservationTime(DATA_PROVIDER);
    assertNotEquals(info, other);
    other.setObservationTime(null);
    assertNotEquals(info, other);
    setFields(other);
    other.setRequiredPermissions(Collections.<String> emptySet());
    assertNotEquals(info, other);
    setFields(other);
    other.setTimeSeriesObjectId(UID.getObjectId());
    assertNotEquals(info, other);
    other.setTimeSeriesObjectId(null);
    assertNotEquals(info, other);
    setFields(other);
    other.setUniqueId(UniqueId.of(UID.getScheme(), "other"));
    assertNotEquals(info, other);
    other.setUniqueId(null);
    assertNotEquals(info, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    setFields(info);
    assertEquals(info.propertyNames().size(), 9);
    final Meta bean = info.metaBean();
    assertEquals(bean.dataField().get(info), DATA_FIELD);
    assertEquals(bean.dataProvider().get(info), DATA_PROVIDER);
    assertEquals(bean.dataSource().get(info), DATA_SOURCE);
    assertEquals(bean.externalIdBundle().get(info), EIDS);
    assertEquals(bean.name().get(info), NAME);
    assertEquals(bean.observationTime().get(info), OBS_TIME);
    assertEquals(bean.requiredPermissions().get(info), PERMISSIONS);
    assertEquals(bean.timeSeriesObjectId().get(info), OID);
    assertEquals(bean.uniqueId().get(info), UID);
    assertEquals(info.property("dataField").get(), DATA_FIELD);
    assertEquals(info.property("dataProvider").get(), DATA_PROVIDER);
    assertEquals(info.property("dataSource").get(), DATA_SOURCE);
    assertEquals(info.property("externalIdBundle").get(), EIDS);
    assertEquals(info.property("name").get(), NAME);
    assertEquals(info.property("observationTime").get(), OBS_TIME);
    assertEquals(info.property("requiredPermissions").get(), PERMISSIONS);
    assertEquals(info.property("timeSeriesObjectId").get(), OID);
    assertEquals(info.property("uniqueId").get(), UID);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    setFields(info);
    assertEncodeDecodeCycle(ManageableHistoricalTimeSeriesInfo.class, info);
  }

  private static void setFields(final ManageableHistoricalTimeSeriesInfo info) {
    info.setDataField(DATA_FIELD);
    info.setDataProvider(DATA_PROVIDER);
    info.setDataSource(DATA_SOURCE);
    info.setExternalIdBundle(EIDS);
    info.setName(NAME);
    info.setObservationTime(OBS_TIME);
    info.setRequiredPermissions(PERMISSIONS);
    info.setTimeSeriesObjectId(OID);
    info.setUniqueId(UID);
  }

}
