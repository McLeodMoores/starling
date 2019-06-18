/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.mxbean;

/**
 * Data of a distributor trace.
 */
public class DistributorTrace {

  private final String _jmsTopic;
  private final String _expiry;
  private final boolean _hasExpired;
  private final boolean _isPersistent;
  private final long _messagesSent;

  /**
   * @param jmsTopic
   *          the topic
   * @param expiry
   *          the expiry
   * @param hasExpired
   *          true if the subscription has expired
   * @param persistent
   *          true if the subscription is persistent
   * @param messagesSent
   *          the number of messages sent
   */
  public DistributorTrace(final String jmsTopic, final String expiry, final boolean hasExpired, final boolean persistent, final long messagesSent) {
    _jmsTopic = jmsTopic;
    _expiry = expiry;
    _hasExpired = hasExpired;
    _isPersistent = persistent;
    _messagesSent = messagesSent;
  }

  /**
   * Gets the JMS topic.
   *
   * @return the JMS topic
   */
  public String getJmsTopic() {
    return _jmsTopic;
  }

  /**
   * Gets the expiry.
   *
   * @return the expiry.
   */
  public String getExpiry() {
    return _expiry;
  }

  /**
   * Return true if the subscription has expired.
   *
   * @return true if the subscription has expired
   */
  public boolean isHasExpired() {
    return _hasExpired;
  }

  /**
   * Return true if the subscription is persistent.
   *
   * @return true if the subscription is persistent
   */
  public boolean isPersistent() {
    return _isPersistent;
  }

  /**
   * Gets the number of messages sent.
   * 
   * @return the number of messages sent
   */
  public long getMessagesSent() {
    return _messagesSent;
  }

}
