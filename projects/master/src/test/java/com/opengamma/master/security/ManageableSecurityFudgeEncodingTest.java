/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableSecurityFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests the simplest possible manageable security.
   */
  public void testBasic() {
    final ManageableSecurity object = new ManageableSecurity("Dummy");
    assertEncodeDecodeCycle(ManageableSecurity.class, object);
  }

  /**
   * Tests a security with identifiers.
   */
  public void testFull() {
    final UniqueId uid = UniqueId.of("A", "123");
    final ExternalIdBundle bundle = ExternalIdBundle.of("X", "Y");
    final ManageableSecurity object = new ManageableSecurity(uid, "OpenGamma", "Dummy", bundle);
    object.setRequiredPermissions(Sets.newHashSet("perm1", "perm2"));
    assertEncodeDecodeCycle(ManageableSecurity.class, object);
  }

}
