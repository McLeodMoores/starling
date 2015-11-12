/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import com.opengamma.util.ArgumentChecker;

/**
 * A target key for access to results associated with portfolio nodes (i.e. aggregates).  The node is addressed using a filesystem style
 * path composed of the name of each node (empty if necessary) separated with forward slashes.
 */
public final class PortfolioNodeTargetKey implements TargetKey {
  private final String _nodePath;

  private PortfolioNodeTargetKey(final String nodePath) {
    _nodePath = ArgumentChecker.notNull(nodePath, "nodePath");
  }

  /**
   * Static factory method used to create instances
   * @param nodePath  the name of each node in the path separated with forward slashes (e.g. Root/Sub-Node/Sub-Sub-Node)
   * @return the portfolio node target key, not null
   */
  public static PortfolioNodeTargetKey of(final String nodePath) {
    return new PortfolioNodeTargetKey(nodePath);
  }

  /**
   * @return the node path - the name of each node in the path separated with forward slashes (e.g. Root/Sub-Node/Sub-Sub-Node)
   */
  public String getNodePath() {
    return _nodePath;
  }

  /**
   * @return a hashCode based on the object
   */
  public int hashCode() {
    return _nodePath.hashCode();
  }

  /**
   * Test equality with another object
   * @param o  the other object
   * @return true, if the objects are equivalent
   */
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof PortfolioNodeTargetKey)) {
      return false;
    }
    PortfolioNodeTargetKey other = (PortfolioNodeTargetKey) o;
    return _nodePath.equals(other._nodePath);
  }

  /**
   * @return a string representation of the key
   */
  public String toString() {
    return "PortfolioNodeTargetKey[" + _nodePath + "]";
  }
}
