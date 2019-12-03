/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ServiceContext}.
 */
@Test(groups = TestGroup.UNIT)
public class ServiceContextTest {
  private static final ConfigSource MOCK_CONFIG_SOURCE = Mockito.mock(ConfigSource.class);
  private static final ConfigSource MOCK_CONFIG_SOURCE2 = Mockito.mock(ConfigSource.class);
  private static final SecuritySource MOCK_SECURITY_SOURCE = Mockito.mock(SecuritySource.class);

  /**
   * Tests that the class cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateServiceHandlesNullClass() {
    ServiceContext.of(null, new Object());
  }

  /**
   * Tests that the service cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateServiceHandlesNullObject() {
    ServiceContext.of(ConfigSource.class, null);
  }

  /**
   * Tests that the map of services cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateServiceWithMapHandlesNullClass() {
    ServiceContext.of(null);
  }

  /**
   * Tests that a service cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateServiceWithMapHandlesNullKeys() {
    final Map<Class<?>, Object> services = new HashMap<>();
    services.put(ConfigSource.class, null);
    ServiceContext.of(services);
  }

  /**
   * Tests that a class cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateServiceWithMapHandlesNullValues() {
    final Map<Class<?>, Object> services = new HashMap<>();
    services.put(null, new Object());
    ServiceContext.of(services);
  }

  /**
   * Tests that the class and service object must match.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testCreateServiceWithIncorrectTypeIsDetected() {
    // Generics prevent ServiceContext.of(ConfigSource.class, MOCK_SECURITY_SOURCE)
    // from compiling. This test ensures we get equivalent safety when we configure
    // via a Map (where the generics can't help).
    final Map<Class<?>, Object> services = new HashMap<>();
    services.put(ConfigSource.class, MOCK_SECURITY_SOURCE);
    ServiceContext.of(services);
  }

  /**
   * Tests the retrieval of a service.
   */
  public void testCreateServiceWorks() {
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    assertThat(context.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE));
  }

  /**
   * Tests the retrieval of services.
   */
  public void testCreateServiceWithMapWorks() {
    final Map<Class<?>, Object> services = ImmutableMap.<Class<?>, Object>of(
        ConfigSource.class, MOCK_CONFIG_SOURCE,
        SecuritySource.class, MOCK_SECURITY_SOURCE);
    final ServiceContext context = ServiceContext.of(services);

    assertThat(context.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE));
    assertThat(context.get(SecuritySource.class), is(MOCK_SECURITY_SOURCE));
  }

  /**
   * Tests that a service can be added.
   */
  public void testAddingServiceWorks() {
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    final ServiceContext context2 = context.with(SecuritySource.class, MOCK_SECURITY_SOURCE);

    assertThat(context2.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE));
    assertThat(context2.get(SecuritySource.class), is(MOCK_SECURITY_SOURCE));
  }

  /**
   * Tests that a null class cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddingServiceWithNullClassIsHandled() {
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    context.with(null, new Object());
  }

  /**
   * Tests that a null source cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddingServiceWithNullObjectIsHandled() {
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    context.with(ConfigSource.class, null);
  }

  /**
   * Tests that the map cannot have null values.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddServiceWithMapHandlesNullValues() {
    final Map<Class<?>, Object> services = new HashMap<>();
    services.put(ConfigSource.class, null);
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    context.with(services);
  }

  /**
   * Tests that the map cannot have null keys.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddServiceWithMapHandlesNullKeys() {
    final Map<Class<?>, Object> services = new HashMap<>();
    services.put(null, new Object());
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    context.with(services);
  }

  /**
   * Tests that a service can be updated.
   */
  public void testUpdatingServiceWorks() {
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    final ServiceContext context2 = context.with(ConfigSource.class, MOCK_CONFIG_SOURCE2);

    assertThat(context.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE));
    assertThat(context2.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE2));
  }

  /**
   * Tests that services can be updated using a map.
   */
  public void testUpdatingServiceWithMapWorks() {
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    final Map<Class<?>, Object> services = ImmutableMap.<Class<?>, Object>of(
        ConfigSource.class, MOCK_CONFIG_SOURCE2,
        SecuritySource.class, MOCK_SECURITY_SOURCE);
    final ServiceContext context2 = context.with(services);

    assertThat(context.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE));
    assertThat(context2.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE2));
    assertThat(context2.get(SecuritySource.class), is(MOCK_SECURITY_SOURCE));
  }

  /**
   * Tests that a non-existent service cannot be retrieved.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testServiceDoesNotExist() {
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    context.get(SecuritySource.class);
  }

  /**
   * Tests that a null class is not allowed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullClass() {
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    context.contains(null);
  }

  /**
   * Tests the contains method.
   */
  public void testContains() {
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE).with(SecuritySource.class, MOCK_SECURITY_SOURCE);
    assertTrue(context.contains(SecuritySource.class));
    assertFalse(context.contains(ConventionSource.class));
    assertTrue(context.contains(ConfigSource.class));
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    final ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE).with(SecuritySource.class, MOCK_SECURITY_SOURCE);
    assertEquals(context.toString(), "ServiceContext[size=2]");
  }
}
