/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility to build a decorated bundle manager for development bundles.
 */
public class DevBundleBuilder {

  /**
   * Maximum number of {@code @imports} allowed in IE.
   */
  public static final int MAX_IMPORTS = 31;
  /** The maximum level 1 size for IE. */
  private static final int LEVEL1_SIZE = MAX_IMPORTS * MAX_IMPORTS;
  /** The maximum level 2 size for IE. */
  private static final int LEVEL2_SIZE = MAX_IMPORTS * MAX_IMPORTS * MAX_IMPORTS;

  /**
   * The bundle manager.
   */
  private final BundleManager _bundleManager;

  /**
   * Creates an instance.
   *
   * @param bundleManager
   *          the bundle manger not null
   */
  public DevBundleBuilder(final BundleManager bundleManager) {
    ArgumentChecker.notNull(bundleManager, "bundleManager");
    _bundleManager = bundleManager;
  }

  // -------------------------------------------------------------------------
  /**
   * Decorates the original bundle manager with a version for development.
   *
   * @return a new bundle manager for development, not null
   */
  public BundleManager getDevBundleManager() {
    final BundleManager devBundleManager = new BundleManager();
    final Set<String> bundleNames = _bundleManager.getBundleIds();
    for (final String bundleId : bundleNames) {
      final Bundle bundle = _bundleManager.getBundle(bundleId);
      final List<Fragment> allFragments = bundle.getAllFragments();
      if (allFragments.size() > LEVEL2_SIZE) {
        throw new IllegalStateException("DevBundleBuilder can only support " + LEVEL2_SIZE + " maximum fragments");
      }
      buildVirtualBundles(devBundleManager, bundleId, allFragments);
    }
    return devBundleManager;
  }

  private static void buildVirtualBundles(final BundleManager bundleManager, final String bundleId, final List<Fragment> fragments) {
    final long fragmentSize = fragments.size();
    if (fragmentSize <= MAX_IMPORTS) {
      final Bundle rootNode = new Bundle(bundleId);
      rootNode.getChildNodes().addAll(fragments);
      bundleManager.addBundle(rootNode);
    }
    if (fragmentSize > MAX_IMPORTS && fragmentSize <= LEVEL1_SIZE) {
      buildLevelOneBundles(bundleManager, bundleId, fragments);
    }
    if (fragmentSize > LEVEL1_SIZE && fragmentSize <= LEVEL2_SIZE) {
      buildLevelTwoBundles(bundleManager, bundleId, fragments);
    }
  }

  private static void buildLevelTwoBundles(final BundleManager bundleManager, final String bundleId, final List<Fragment> fragments) {
    final Map<Integer, List<Fragment>> parentFragmentMap = split(fragments);
    final Bundle rootNode = new Bundle(bundleId);
    for (final Entry<Integer, List<Fragment>> parentEntry : parentFragmentMap.entrySet()) {
      final String parentId = String.valueOf(parentEntry.getKey());
      final String parentName = buildBundleName(bundleId, parentId, null);
      final Bundle parentBundle = new Bundle(parentName);
      final Map<Integer, List<Fragment>> childFragmentMap = split(parentEntry.getValue());
      for (final Entry<Integer, List<Fragment>> childEntry : childFragmentMap.entrySet()) {
        final String childName = buildBundleName(bundleId, parentId, String.valueOf(childEntry.getKey()));
        final Bundle childBundle = new Bundle(childName);
        for (final Fragment fragment : childEntry.getValue()) {
          childBundle.addChildNode(fragment);
        }
        parentBundle.addChildNode(childBundle);
      }
      rootNode.addChildNode(parentBundle);
    }
    bundleManager.addBundle(rootNode);
  }

  private static void buildLevelOneBundles(final BundleManager bundleManager, final String bundleId, final List<Fragment> fragments) {
    final Map<Integer, List<Fragment>> fragmentMap = split(fragments);
    final Bundle rootNode = new Bundle(bundleId);
    for (final Entry<Integer, List<Fragment>> entry : fragmentMap.entrySet()) {
      final String bundleName = buildBundleName(bundleId, String.valueOf(entry.getKey()), null);
      final Bundle bundle = new Bundle(bundleName);
      final List<Fragment> fragmentList = entry.getValue();
      for (final Fragment fragment : fragmentList) {
        bundle.addChildNode(fragment);
      }
      rootNode.addChildNode(bundle);
    }
    bundleManager.addBundle(rootNode);
  }

  private static String buildBundleName(final String bundleId, final String parent, final String child) {
    final BundleType type = BundleType.getType(bundleId);
    final StringBuilder buf = new StringBuilder(bundleId.substring(0, bundleId.indexOf(type.getSuffix()) - 1));
    if (parent != null) {
      buf.append("-");
      buf.append(parent);
    }
    if (child != null) {
      buf.append("-");
      buf.append(child);
    }
    buf.append(".").append(type.getSuffix());
    return buf.toString();
  }

  private static Map<Integer, List<Fragment>> split(final List<Fragment> fragments) {
    final Map<Integer, List<Fragment>> result = new TreeMap<>();
    int bundleSize = fragments.size() / MAX_IMPORTS;
    if (fragments.size() % MAX_IMPORTS != 0) {
      ++bundleSize;
    }
    int counter = 0;
    final List<Fragment> current = new ArrayList<>();
    int next = 1;
    for (final Fragment fragment : fragments) {
      current.add(fragment);
      if (++counter % bundleSize == 0) {
        result.put(next++, new ArrayList<>(current));
        current.clear();
      }
    }
    if (!current.isEmpty()) {
      result.put(next, current);
    }
    return result;
  }

}
