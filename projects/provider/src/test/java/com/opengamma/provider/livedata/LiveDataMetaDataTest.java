/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.livedata;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalScheme;
import com.opengamma.provider.AbstractBeanTestCase;

/**
 * Tests for {@link LiveDataMetaData}.
 */
public class LiveDataMetaDataTest extends AbstractBeanTestCase {
  private static final List<ExternalScheme> SUPPORTED_SCHEMES = Arrays.asList(ExternalScheme.of("scheme1"), ExternalScheme.of("scheme2"));
  private static final LiveDataServerType SERVER_TYPE = LiveDataServerTypes.STANDARD;
  private static final String DESCRIPTION = "Live data server";
  private static final URI CONNECTION_URI;
  private static final URI JMS_BROKER_URI;
  static {
    try {
      CONNECTION_URI = new URI("path/to/connection");
      JMS_BROKER_URI = new URI("path/to/jms/broker");
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }
  private static final String JMS_QUEUE = "subscriptionQueue";
  private static final String JMS_ENTITLEMENT = "entitlementTopic";
  private static final String JMS_HEARTBEAT = "heartbeatTopic";

  /**
   * Tests that the supported schemes cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSupportedSchemes() {
    new LiveDataMetaData(null, SERVER_TYPE, DESCRIPTION);
  }

  /**
   * Tests that the server type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullServerType() {
    new LiveDataMetaData(SUPPORTED_SCHEMES, null, DESCRIPTION);
  }

  /**
   * Tests that the description cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDescription() {
    new LiveDataMetaData(SUPPORTED_SCHEMES, SERVER_TYPE, null);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(LiveDataMetaData.class,
        Arrays.asList("supportedSchemes", "serverType", "description", "connectionUri", "jmsBrokerUri", "jmsSubscriptionQueue", "jmsEntitlementTopic",
            "jmsHeartbeatTopic"),
        Arrays.asList(SUPPORTED_SCHEMES, SERVER_TYPE, DESCRIPTION, CONNECTION_URI, JMS_BROKER_URI, JMS_QUEUE, JMS_ENTITLEMENT, JMS_HEARTBEAT),
        Arrays.asList(Collections.<ExternalScheme> singletonList(ExternalScheme.of("scheme1")), LiveDataServerTypes.COGDA, JMS_QUEUE, JMS_BROKER_URI,
            CONNECTION_URI, JMS_ENTITLEMENT, JMS_HEARTBEAT, DESCRIPTION));
  }
}
