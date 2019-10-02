/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.async.BlockingOperation;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link UnionMarketDataAvailability} class.
 */
@Test(groups = TestGroup.UNIT)
public class UnionMarketDataAvailabilityTest {

  /**
   *
   */
  private static class BlockingDataProvider implements MarketDataAvailabilityProvider {

    @Override
    public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target,
        final ValueRequirement desiredValue) throws MarketDataNotSatisfiableException {
      if (target instanceof ExternalBundleIdentifiable) {
        if (((ExternalBundleIdentifiable) target).getExternalIdBundle().contains(ExternalId.of("C", "Blocking"))) {
          throw BlockingOperation.block();
        }
      }
      return null;
    }

    @Override
    public MarketDataAvailabilityFilter getAvailabilityFilter() {
      return new MarketDataAvailabilityFilter() {

        @Override
        public boolean isAvailable(final ComputationTargetSpecification targetSpec, final Object target,
            final ValueRequirement desiredValue) throws MarketDataNotSatisfiableException {
          if (target instanceof ExternalBundleIdentifiable) {
            if (((ExternalBundleIdentifiable) target).getExternalIdBundle().contains(ExternalId.of("C", "Blocking"))) {
              throw BlockingOperation.block();
            }
          }
          return false;
        }

        @Override
        public MarketDataAvailabilityProvider withProvider(final MarketDataAvailabilityProvider provider) {
          return BlockingDataProvider.this;
        }

      };
    }

    @Override
    public Serializable getAvailabilityHintKey() {
      return getClass();
    }

  }

  /**
   * @return the filter
   */
  protected MarketDataAvailabilityProvider createProvider1() {
    final FixedMarketDataAvailabilityProvider a = new FixedMarketDataAvailabilityProvider();
    final FixedMarketDataAvailabilityProvider b = new FixedMarketDataAvailabilityProvider();
    final MarketDataAvailabilityProvider c = new BlockingDataProvider();
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get();
    a.addAvailableData(ExternalId.of("A", "Present"),
        new ValueSpecification("Foo", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Y", "A")), properties));
    a.addAvailableData(ExternalId.of("A", "Missing"),
        new ValueSpecification("Foo", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Y", "A")), properties));
    b.addAvailableData(ExternalId.of("B", "Present"),
        new ValueSpecification("Foo", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Y", "B")), properties));
    b.addAvailableData(ExternalId.of("B", "Missing"),
        new ValueSpecification("Foo", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Y", "B")), properties));
    return new UnionMarketDataAvailability.Provider(Arrays.asList(c, a, b));
  }

  /**
   * @return the filter
   */
  protected MarketDataAvailabilityFilter createFilter1() {
    return createProvider1().getAvailabilityFilter();
  }

  /**
   * @return the filter
   */
  protected MarketDataAvailabilityProvider createProvider2() {
    final MarketDataAvailabilityFilter filter = createFilter1();
    return filter.withProvider(new MarketDataAvailabilityProvider() {

      @Override
      public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target,
          final ValueRequirement desiredValue) throws MarketDataNotSatisfiableException {
        return new ValueSpecification("Bar", targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
      }

      @Override
      public MarketDataAvailabilityFilter getAvailabilityFilter() {
        return filter;
      }

      @Override
      public Serializable getAvailabilityHintKey() {
        return getClass();
      }

    });
  }

  /**
   * @return the filter
   */
  protected MarketDataAvailabilityFilter createFilter2() {
    return createProvider2().getAvailabilityFilter();
  }

  /**
   * @return the providers
   */
  @DataProvider
  public Object[][] providers() {
    return new Object[][] { new Object[] { createProvider1() }, new Object[] { createProvider2() } };
  }

  /**
   * @return the filters
   */
  @DataProvider
  public Object[][] filters() {
    return new Object[][] { new Object[] { createFilter1() }, new Object[] { createFilter2() } };
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(dataProvider = "providers")
  public void testGetAvailabilityAvailable(final MarketDataAvailabilityProvider availability) {
    try {
      BlockingOperation.off();
      final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("X", "0"));
      final ValueRequirement desiredValue = new ValueRequirement("Foo", targetSpec);
      assertNotNull(availability.getAvailability(targetSpec,
          ExternalId.of("A", "Present"), desiredValue));
      assertNotNull(availability.getAvailability(targetSpec,
          ExternalId.of("B", "Present"), desiredValue));
      assertNotNull(availability.getAvailability(targetSpec,
          ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Present")), desiredValue));
      assertNotNull(availability.getAvailability(targetSpec,
          ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Present"), ExternalId.of("C", "Absent")), desiredValue));
      assertNotNull(availability.getAvailability(targetSpec,
          ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Present"), ExternalId.of("C", "Blocking")), desiredValue));
      assertNotNull(availability.getAvailability(targetSpec,
          ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Missing"), ExternalId.of("C", "Blocking")), desiredValue));
      assertNotNull(availability.getAvailability(targetSpec,
          ExternalIdBundle.of(ExternalId.of("A", "Missing"), ExternalId.of("B", "Present")), desiredValue));
    } finally {
      BlockingOperation.on();
    }
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(dataProvider = "filters")
  public void testIsAvailableAvailable(final MarketDataAvailabilityFilter availability) {
    try {
      BlockingOperation.off();
      final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("X", "0"));
      final ValueRequirement desiredValue = new ValueRequirement("Foo", targetSpec);
      assertTrue(availability.isAvailable(targetSpec,
          ExternalId.of("A", "Present"), desiredValue));
      assertTrue(availability.isAvailable(targetSpec,
          ExternalId.of("B", "Present"), desiredValue));
      assertTrue(availability.isAvailable(targetSpec,
          ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Present")), desiredValue));
      assertTrue(availability.isAvailable(targetSpec,
          ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Present"), ExternalId.of("C", "Absent")), desiredValue));
      assertTrue(availability.isAvailable(targetSpec,
          ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Present"), ExternalId.of("C", "Blocking")), desiredValue));
      assertTrue(availability.isAvailable(targetSpec,
          ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Missing"), ExternalId.of("C", "Blocking")), desiredValue));
      assertTrue(availability.isAvailable(targetSpec,
          ExternalIdBundle.of(ExternalId.of("A", "Missing"), ExternalId.of("B", "Present")), desiredValue));
    } finally {
      BlockingOperation.on();
    }
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(dataProvider = "providers")
  public void testGetAvailabilityAbsent(final MarketDataAvailabilityProvider availability) {
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("X", "0"));
    final ValueRequirement desiredValue = new ValueRequirement("Foo", targetSpec);
    assertNull(availability.getAvailability(targetSpec, ExternalId.of("A", "Absent"), desiredValue));
    assertNull(availability.getAvailability(targetSpec, ExternalId.of("B", "Absent"), desiredValue));
    assertNull(availability.getAvailability(targetSpec, ExternalId.of("C", "Absent"), desiredValue));
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(dataProvider = "filters")
  public void testIsAvailableAbsent(final MarketDataAvailabilityFilter availability) {
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("X", "0"));
    final ValueRequirement desiredValue = new ValueRequirement("Foo", targetSpec);
    assertFalse(availability.isAvailable(targetSpec, ExternalId.of("A", "Absent"), desiredValue));
    assertFalse(availability.isAvailable(targetSpec, ExternalId.of("B", "Absent"), desiredValue));
    assertFalse(availability.isAvailable(targetSpec, ExternalId.of("C", "Absent"), desiredValue));
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(dataProvider = "providers")
  public void testGetAvailabilityMissingA(final MarketDataAvailabilityProvider availability) {
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    assertNotNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalId.of("A", "Missing"), desiredValue));
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(dataProvider = "filters")
  public void testIsAvailableMissingA(final MarketDataAvailabilityFilter availability) {
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    assertTrue(availability.isAvailable(ComputationTargetSpecification.NULL, ExternalId.of("A", "Missing"), desiredValue));
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(dataProvider = "providers")
  public void testGetAvailabilityMissingB(final MarketDataAvailabilityProvider availability) {
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    assertNotNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Missing"),
        ExternalId.of("B", "Missing")), desiredValue));
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(dataProvider = "filters")
  public void testIsAvailableMissingB(final MarketDataAvailabilityFilter availability) {
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    assertTrue(availability.isAvailable(ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Missing"),
        ExternalId.of("B", "Missing")), desiredValue));
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(dataProvider = "providers")
  public void testGetAvailabilityMissingC(final MarketDataAvailabilityProvider availability) {
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    assertNotNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Absent"),
        ExternalId.of("B", "Missing")), desiredValue));
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(dataProvider = "filters")
  public void testIsAvailableMissingC(final MarketDataAvailabilityFilter availability) {
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    assertTrue(availability.isAvailable(ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Absent"),
        ExternalId.of("B", "Missing")), desiredValue));
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(expectedExceptions = { BlockingOperation.class }, dataProvider = "providers")
  public void testGetAvailabilityNoneAvailableBlockingOperationA(final MarketDataAvailabilityProvider availability) {
    try {
      BlockingOperation.off();
      final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
      assertNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalId.of("C", "Blocking").toBundle(), desiredValue));
    } finally {
      BlockingOperation.on();
    }
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(expectedExceptions = { BlockingOperation.class }, dataProvider = "filters")
  public void testIsAvailableNoneAvailableBlockingOperationA(final MarketDataAvailabilityFilter availability) {
    try {
      BlockingOperation.off();
      final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
      assertFalse(availability.isAvailable(ComputationTargetSpecification.NULL, ExternalId.of("C", "Blocking").toBundle(), desiredValue));
    } finally {
      BlockingOperation.on();
    }
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(expectedExceptions = { BlockingOperation.class }, dataProvider = "providers")
  public void testGetAvailabilityNoneAvailableBlockingOperationB(final MarketDataAvailabilityProvider availability) {
    try {
      BlockingOperation.off();
      final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
      assertNull(availability.getAvailability(
          ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Absent"), ExternalId.of("C", "Blocking")), desiredValue));
    } finally {
      BlockingOperation.on();
    }
  }

  /**
   * @param availability
   *          the filter
   */
  @Test(expectedExceptions = { BlockingOperation.class }, dataProvider = "filters")
  public void testIsAvailableNoneAvailableBlockingOperationB(final MarketDataAvailabilityFilter availability) {
    try {
      BlockingOperation.off();
      final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
      assertFalse(availability.isAvailable(
          ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Absent"), ExternalId.of("C", "Blocking")), desiredValue));
    } finally {
      BlockingOperation.on();
    }
  }

}
