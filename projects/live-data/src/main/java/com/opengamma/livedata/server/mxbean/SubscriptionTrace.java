/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.mxbean;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Data of a subscription trace.
 */
public class SubscriptionTrace {

  /**
   * The identifier for the subscription being traced.
   */
  private final String _identifier;
  /**
   * Creation time of the subscription. Held as a string for compatibility with the MX Bean.
   */
  private final String _created;
  /**
   * The distributor traces.
   */
  private final Set<DistributorTrace> _distributors;
  /**
   * The last value.
   */
  private final String _lastValues;

  /**
   * Creates an instance.
   *
   * @param identifier  the identifier
   */
  public SubscriptionTrace(final String identifier) {
    this(identifier, "N/A", ImmutableSet.<DistributorTrace>of(), "N/A");
  }

  /**
   * Creates an instance.
   *
   * @param identifier
   *          the identifier
   * @param created
   *          the subscription creation time as a string
   * @param distributors
   *          the distributors
   * @param lastValues
   *          the last value
   */
  public SubscriptionTrace(final String identifier, final String created, final Set<DistributorTrace> distributors, final String lastValues) {
    _identifier = identifier;
    _created = created;
    _distributors = distributors;
    _lastValues = lastValues;
  }

  /**
   * Gets the identifier.
   *
   * @return the identifier
   */
  public String getIdentifier() {
    return _identifier;
  }

  /**
   * Gets the subscription creation time.
   *
   * @return the creation time
   */
  public String getCreated() {
    return _created;
  }

  /**
   * Gets the distributors.
   *
   * @return the distributors
   */
  public Set<DistributorTrace> getDistributors() {
    return _distributors;
  }

  /**
   * Gets the last value.
   *
   * @return the last value
   */
  public String getLastValues() {
    return _lastValues;
  }

}
