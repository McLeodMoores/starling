/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument.Meta;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesInfoDocument}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesInfoDocumentTest extends AbstractFudgeBuilderTestCase {
  private static final ManageableHistoricalTimeSeriesInfo INFO = new ManageableHistoricalTimeSeriesInfo();
  private static final UniqueId UID = UniqueId.of("uid", "1");
  static {
    INFO.setDataField("field");
    INFO.setName("name");
    INFO.setUniqueId(UID);
  }

  /**
   * Test the object.
   */
  @Test
  public void testObject() {
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setInfo(INFO);
    HistoricalTimeSeriesInfoDocument other = new HistoricalTimeSeriesInfoDocument(INFO);
    assertEquals(doc, other);
    assertEquals(doc.hashCode(), other.hashCode());
    assertEquals(doc.toString(),
        "HistoricalTimeSeriesInfoDocument{versionFromInstant=null, versionToInstant=null, "
            + "correctionFromInstant=null, correctionToInstant=null, info=ManageableHistoricalTimeSeriesInfo{uniqueId=uid~1, "
            + "externalIdBundle=null, name=name, dataField=field, dataSource=null, dataProvider=null, observationTime=null, "
            + "timeSeriesObjectId=null, requiredPermissions=[]}}");
    assertEquals(doc.getUniqueId(), INFO.getUniqueId());
    other = new HistoricalTimeSeriesInfoDocument(new ManageableHistoricalTimeSeriesInfo());
    assertNotEquals(doc, other);
    // note no clone of info on construction or set
    other.setInfo(INFO.clone());
    other.setUniqueId(UniqueId.of("uid", "2"));
    assertNotEquals(doc, other);
    assertEquals(other.getInfo().getUniqueId(), UniqueId.of("uid", "2"));
  }

  /**
   * Test the bean.
   */
  @Test
  public void testBean() {
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(INFO);
    assertEquals(doc.propertyNames().size(), 6);
    final Meta bean = doc.metaBean();
    assertNull(bean.correctionFromInstant().get(doc));
    assertNull(bean.correctionToInstant().get(doc));
    assertEquals(bean.info().get(doc), INFO);
    assertEquals(bean.uniqueId().get(doc), UID);
    assertNull(bean.versionFromInstant().get(doc));
    assertNull(bean.versionToInstant().get(doc));
    assertNull(doc.property("correctionFromInstant").get());
    assertNull(doc.property("correctionToInstant").get());
    assertEquals(doc.property("info").get(), INFO);
    assertEquals(doc.property("uniqueId").get(), UID);
    assertNull(doc.property("versionFromInstant").get());
    assertNull(doc.property("versionToInstant").get());
  }
}
