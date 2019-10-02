/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.net.URI;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test DevBundleBuilder.
 */
@Test(groups = TestGroup.UNIT)
public class DevBundleBuilderTest {

  /**
   * @throws Exception
   *           if there is an unexpected problem
   */
  public void testBuilder() throws Exception {
    final BundleManager bundleManager = new BundleManager();

    final Bundle testBundle = new Bundle("A.css");
    for (int i = 1; i <= 100; i++) {
      final URI uri = new URI("A" + i + ".css");
      testBundle.addChildNode(new Fragment(uri, "/" + uri.toString()));
    }
    bundleManager.addBundle(testBundle);

    final DevBundleBuilder devBundleBuilder = new DevBundleBuilder(bundleManager);
    final BundleManager devBundleManager = devBundleBuilder.getDevBundleManager();

    assertNotNull(devBundleManager);

    final Bundle bundle = devBundleManager.getBundle("A.css");
    assertEquals("A.css", bundle.getId());
    final List<BundleNode> childNodes = bundle.getChildNodes();
    assertTrue(childNodes.size() <= DevBundleBuilder.MAX_IMPORTS);
    for (final BundleNode bundleNode : childNodes) {
      assertBundleNode(bundleNode);
    }
    assertEquals(testBundle.getAllFragments(), bundle.getAllFragments());
  }

  private void assertBundleNode(final BundleNode bundleNode) {
    if (bundleNode instanceof Bundle) {
      final Bundle testBundle = (Bundle) bundleNode;
      final List<BundleNode> childNodes = testBundle.getChildNodes();
      assertTrue(childNodes.size() <= DevBundleBuilder.MAX_IMPORTS);
      for (final BundleNode childNode : childNodes) {
        assertBundleNode(childNode);
      }
    }
  }

}
