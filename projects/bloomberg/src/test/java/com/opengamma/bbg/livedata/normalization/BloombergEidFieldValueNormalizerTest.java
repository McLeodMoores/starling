/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.livedata.permission.PermissionUtils;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test EID field name and value normalization in liveData and snapshot.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergEidFieldValueNormalizerTest {

  public void normalizeEidNameAndValue() {
    final BloombergEidFieldValueNormalizer normalizer = new BloombergEidFieldValueNormalizer();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(BloombergConstants.EID_LIVE_DATA_FIELD, 10);
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    msg.add(BloombergConstants.EID_LIVE_DATA_FIELD, 20);
    msg.add(BloombergConstants.EID_DATA.toString(), 30);
    msg.add(BloombergConstants.EID_DATA.toString(), 40);

    final MutableFudgeMsg normalized = normalizer.apply(msg, "test", new FieldHistoryStore());
    assertEquals(6, normalized.getAllFields().size());
    final List<FudgeField> eidLiveData = normalized.getAllByName(BloombergConstants.EID_LIVE_DATA_FIELD);
    assertTrue(eidLiveData.isEmpty());

    final List<FudgeField> eidRefData = normalized.getAllByName(BloombergConstants.EID_DATA.toString());
    assertTrue(eidRefData.isEmpty());

    final List<FudgeField> permissions = normalized.getAllByName(PermissionUtils.LIVE_DATA_PERMISSION_FIELD);
    assertEquals(4, permissions.size());
    final List<String> permissionValues = Lists.newArrayList();
    for (final FudgeField fudgeField : permissions) {
      permissionValues.add((String) fudgeField.getValue());
    }
    assertTrue(permissionValues.contains("Data:Bloomberg:EID:10"));
    assertTrue(permissionValues.contains("Data:Bloomberg:EID:20"));
    assertTrue(permissionValues.contains("Data:Bloomberg:EID:30"));
    assertTrue(permissionValues.contains("Data:Bloomberg:EID:40"));
  }
}
