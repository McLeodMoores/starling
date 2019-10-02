/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ComputationTargetRequirement} class.
 */
@Test(groups = TestGroup.UNIT)
public class ComputationTargetRequirementTest {

  /**
   *
   */
  public void testConstructorId() {
    final ComputationTargetRequirement requirement = new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("Foo", "Bar"));
    assertEquals(requirement.getType(), ComputationTargetType.PRIMITIVE);
    assertEquals(requirement.getIdentifiers(), ExternalId.of("Foo", "Bar").toBundle());
  }

  /**
   *
   */
  @Test(expectedExceptions = { AssertionError.class })
  public void testConstructorIdInvalidType1() {
    new ComputationTargetRequirement(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY), ExternalId.of("Foo", "Bar"));
  }

  /**
   *
   */
  @Test(expectedExceptions = { AssertionError.class })
  public void testConstructorIdInvalidType2() {
    new ComputationTargetRequirement(null, ExternalId.of("Foo", "Bar"));
  }

  /**
   *
   */
  public void testConstructorIdNullValid() {
    final ComputationTargetRequirement requirement = new ComputationTargetRequirement(ComputationTargetType.NULL, (ExternalId) null);
    assertEquals(requirement.getType(), ComputationTargetType.NULL);
    assertEquals(requirement.getIdentifiers(), ExternalIdBundle.EMPTY);
  }

  /**
   *
   */
  @Test(expectedExceptions = { IllegalArgumentException.class })
  public void testConstructorIdNullInvalid1() {
    new ComputationTargetRequirement(ComputationTargetType.POSITION, (ExternalId) null);
  }

  /**
   *
   */
  @Test(expectedExceptions = { IllegalArgumentException.class })
  public void testConstructorIdNullInvalid2() {
    new ComputationTargetRequirement(ComputationTargetType.NULL, ExternalId.of("Foo", "Bar"));
  }

  /**
   *
   */
  public void testConstructorBundle() {
    final ComputationTargetRequirement requirement = new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE,
        ExternalIdBundle.of(ExternalId.of("Foo", "1"), ExternalId.of("Bar", "2")));
    assertEquals(requirement.getType(), ComputationTargetType.PRIMITIVE);
    assertEquals(requirement.getIdentifiers(), ExternalIdBundle.of(ExternalId.of("Foo", "1"), ExternalId.of("Bar", "2")));
  }

  /**
   *
   */
  @Test(expectedExceptions = { IllegalArgumentException.class })
  public void testConstructorBundleInvalid() {
    new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalIdBundle.EMPTY);
  }

  /**
   *
   */
  @Test(expectedExceptions = { AssertionError.class })
  public void testConstructorBundleInvalidType1() {
    new ComputationTargetRequirement(ComputationTargetType.POSITION.containing(ComputationTargetType.SECURITY),
        ExternalIdBundle.of(ExternalId.of("Foo", "1"), ExternalId.of("Bar", "2")));
  }

  /**
   *
   */
  @Test(expectedExceptions = { AssertionError.class })
  public void testConstructorBundleInvalidType2() {
    new ComputationTargetRequirement(null, ExternalIdBundle.of(ExternalId.of("Foo", "1"), ExternalId.of("Bar", "2")));
  }

  /**
   *
   */
  public void testConstructorBundleNullValid1() {
    final ComputationTargetRequirement requirement = new ComputationTargetRequirement(ComputationTargetType.NULL, (ExternalIdBundle) null);
    assertEquals(requirement.getType(), ComputationTargetType.NULL);
    assertEquals(requirement.getIdentifiers(), ExternalIdBundle.EMPTY);
  }

  /**
   *
   */
  public void testConstructorBundleNullValid2() {
    final ComputationTargetRequirement requirement = new ComputationTargetRequirement(ComputationTargetType.NULL, ExternalIdBundle.EMPTY);
    assertEquals(requirement.getType(), ComputationTargetType.NULL);
    assertEquals(requirement.getIdentifiers(), ExternalIdBundle.EMPTY);
  }

  /**
   *
   */
  @Test(expectedExceptions = { IllegalArgumentException.class })
  public void testConstructorBundleNullInvalid1() {
    new ComputationTargetRequirement(ComputationTargetType.SECURITY, (ExternalIdBundle) null);
  }

  /**
   *
   */
  @Test(expectedExceptions = { IllegalArgumentException.class })
  public void testConstructorBundleNullInvalid2() {
    new ComputationTargetRequirement(ComputationTargetType.NULL, ExternalIdBundle.of(ExternalId.of("Foo", "1"), ExternalId.of("Bar", "2")));
  }

  /**
   *
   */
  public void testGetRequirement() {
    final ComputationTargetReference a = new ComputationTargetRequirement(ComputationTargetType.NULL, ExternalIdBundle.EMPTY);
    assertEquals(a.getRequirement(), a);
  }

  /**
   *
   */
  @Test(expectedExceptions = { IllegalStateException.class })
  public void testGetSpecification() {
    final ComputationTargetReference a = new ComputationTargetRequirement(ComputationTargetType.NULL, ExternalIdBundle.EMPTY);
    a.getSpecification();
  }

  /**
   *
   */
  public void testContainingId() {
    final ComputationTargetReference ref = new ComputationTargetRequirement(ComputationTargetType.SECURITY, ExternalId.of("Foo", "Bar"));
    final ComputationTargetReference underlying = ref.containing(ComputationTargetType.SECURITY, ExternalId.of("Foo", "Underlying"));
    assertEquals(underlying.getParent(), ref);
    assertEquals(underlying.getType(), ComputationTargetType.SECURITY.containing(ComputationTargetType.SECURITY));
  }

  /**
   *
   */
  public void testContainingUid() {
    final ComputationTargetReference ref = new ComputationTargetRequirement(ComputationTargetType.SECURITY, ExternalId.of("Foo", "Bar"));
    final ComputationTargetReference underlying = ref.containing(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Underlying"));
    assertEquals(underlying.getParent(), ref);
    assertEquals(underlying.getType(), ComputationTargetType.SECURITY.containing(ComputationTargetType.SECURITY));
  }

}
