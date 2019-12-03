/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region;

import static org.testng.AssertJUnit.fail;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableRegionFudgeEncodingTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ManageableRegionFudgeEncodingTest.class);
  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  /**
   *
   */
  public void test() {
    final ManageableRegion obj = new ManageableRegion();
    obj.setUniqueId(UniqueId.of("U", "1"));
    obj.setExternalIdBundle(ExternalIdBundle.of("A", "B"));
    obj.setClassification(RegionClassification.INDEPENDENT_STATE);
    obj.setParentRegionIds(ImmutableSet.of(UniqueId.of("U", "1"), UniqueId.of("U", "2")));
    obj.setName("Test");
    obj.setFullName("Testland");
    obj.getData().set("P1", "A");
    obj.getData().set("P2", "B");
    testFudgeMessage(obj);
  }

  private static void testFudgeMessage(final ManageableRegion obj) {
    final FudgeSerializer serializer = new FudgeSerializer(FUDGE_CONTEXT);
    FudgeMsg msg = serializer.objectToFudgeMsg(obj);
    LOGGER.debug("ManageableRegion {}", obj);
    LOGGER.debug("Encoded to {}", msg);
    final byte[] bytes = FUDGE_CONTEXT.toByteArray(msg);
    msg = FUDGE_CONTEXT.deserialize(bytes).getMessage();
    LOGGER.debug("Serialised to {}", msg);
    final ManageableRegion decoded = FUDGE_CONTEXT.fromFudgeMsg(ManageableRegion.class, msg);
    LOGGER.debug("Decoded to {}", decoded);
    if (!obj.equals(decoded)) {
      LOGGER.warn("Expected {}", obj);
      LOGGER.warn("Received {}", decoded);
      fail();
    }
  }

}
