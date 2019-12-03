/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.id;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link VersionCorrectionUtils}.
 */
@Test(groups = TestGroup.UNIT)
public class VersionCorrectionUtilsTest {

  /**
   * Tests the behaviour when trying to unlock a version / correction that has not been locked.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testUnlockNotLocked() {
    final VersionCorrection vc = VersionCorrection.of(Instant.now(), Instant.now().plusSeconds(10000));
    VersionCorrectionUtils.unlock(vc);
  }

}
