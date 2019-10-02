/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.Primitive;
import com.opengamma.engine.target.Primitive.ExternalBundleIdentifiablePrimitive;
import com.opengamma.engine.target.Primitive.ExternalIdentifiablePrimitive;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DomainMarketDataAvailabilityFilter} class.
 */
@Test(groups = TestGroup.UNIT)
public class DomainMarketDataAvailabilityFilterTest {

  /**
   * @return the filter
   */
  protected MarketDataAvailabilityFilter create() {
    return new DomainMarketDataAvailabilityFilter(ImmutableSet.of(ExternalScheme.of("Ticker")), ImmutableSet.of("Foo", "Bar"));
  }

  /**
   *
   */
  public void testGetAvailabilityUniqueId() {
    final MarketDataAvailabilityFilter availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new Primitive(UniqueId.of("Security", "Foo"));
    ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertFalse(availability.isAvailable(targetSpec, target, desiredValue));
    desiredValue = new ValueRequirement("Foo", targetSpec);
    assertFalse(availability.isAvailable(targetSpec, target, desiredValue));
  }

  /**
   *
   */
  public void testGetAvailabilityExternalIdNomatch() {
    final MarketDataAvailabilityFilter availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalId.of("BAD", "Foo"));
    ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertFalse(availability.isAvailable(targetSpec, target, desiredValue));
    desiredValue = new ValueRequirement("Foo", targetSpec);
    assertFalse(availability.isAvailable(targetSpec, target, desiredValue));
  }

  /**
   *
   */
  public void testGetAvailabilityExternalIdMatch() {
    final MarketDataAvailabilityFilter availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalId.of("Ticker", "Foo"));
    ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertFalse(availability.isAvailable(targetSpec, target, desiredValue));
    desiredValue = new ValueRequirement("Foo", targetSpec);
    assertTrue(availability.isAvailable(targetSpec, target, desiredValue));
  }

  /**
   *
   */
  public void testGetAvailabilityExternalIdBundleNomatch() {
    final MarketDataAvailabilityFilter availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalBundleIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalIdBundle.of(ExternalId.of("BAD1", "Foo"),
        ExternalId.of("BAD2", "Foo")));
    ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertFalse(availability.isAvailable(targetSpec, target, desiredValue));
    desiredValue = new ValueRequirement("Foo", targetSpec);
    assertFalse(availability.isAvailable(targetSpec, target, desiredValue));
  }

  /**
   *
   */
  public void testGetAvailabilityExternalIdBundleMatch() {
    final MarketDataAvailabilityFilter availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalBundleIdentifiablePrimitive(UniqueId.of("Security", "Foo"),
        ExternalIdBundle.of(ExternalId.of("Ticker", "X"), ExternalId.of("BAD2", "Foo"),
            ExternalId.of("Ticker", "Y")));
    ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertFalse(availability.isAvailable(targetSpec, target, desiredValue));
    desiredValue = new ValueRequirement("Foo", targetSpec);
    assertTrue(availability.isAvailable(targetSpec, target, desiredValue));
  }

}
