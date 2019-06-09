/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.threeten.bp.Instant;

import com.opengamma.livedata.server.LiveDataServerMBean;

/**
 * JMX management of a {@link BloombergLiveDataServer}.
 */
public class BloombergLiveDataServerMBean extends LiveDataServerMBean {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BloombergLiveDataServerMBean.class);

  private final BloombergLiveDataServer _server;

  public BloombergLiveDataServerMBean(final BloombergLiveDataServer server) {
    super(server);
    _server = server;
  }

  @ManagedAttribute(description = "Get the limit on concurrent subscriptions")
  public long getSubscriptionLimit() {
    try {
      return _server.getSubscriptionLimit();
    } catch (final RuntimeException e) {
      LOGGER.error("getConnectionStatus() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute(description = "Set the limit on concurrent subscriptions")
  public void setSubscriptionLimit(final long subscriptionLimit) {
    try {
      _server.setSubscriptionLimit(subscriptionLimit);
    } catch (final RuntimeException e) {
      LOGGER.error("getConnectionStatus() failed", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @ManagedAttribute()
  public long getLastLimitRejectionEpochSeconds() {
    final RejectedDueToSubscriptionLimitEvent rejection = _server.getLastLimitRejection();
    if (rejection == null) {
      return 0;
    }
    return rejection.getInstant().getEpochSecond();
  }

  @ManagedAttribute()
  public long getLastLimitSecondsSince() {
    final RejectedDueToSubscriptionLimitEvent rejection = _server.getLastLimitRejection();
    if (rejection == null) {
      return Long.MAX_VALUE;
    }
    return Instant.now().getEpochSecond() - rejection.getInstant().getEpochSecond();
  }

  @ManagedAttribute()
  public String getLastLimitRejectionTime() {
    final RejectedDueToSubscriptionLimitEvent rejection = _server.getLastLimitRejection();
    if (rejection == null) {
      return null;
    }
    return rejection.getInstant().toString();
  }

  @ManagedAttribute()
  public int getLastLimitRequestSize() {
    final RejectedDueToSubscriptionLimitEvent rejection = _server.getLastLimitRejection();
    if (rejection == null) {
      return -1;
    }
    return rejection.getRequestedSubscriptions();
  }

  @ManagedAttribute()
  public int getLastLimitRequiredEntitlement() {
    final RejectedDueToSubscriptionLimitEvent rejection = _server.getLastLimitRejection();
    if (rejection == null) {
      return -1;
    }
    return rejection.getAfterSubscriptionCount();
  }
}
