/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.beans.Bean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CDSIndexComponentBundleTest extends AbstractBeanTestCase {

  private static final Comparator<CreditDefaultSwapIndexComponent> WEIGHT_COMPARATOR =
      new Comparator<CreditDefaultSwapIndexComponent>() {
    @Override
    public int compare(final CreditDefaultSwapIndexComponent o1, final CreditDefaultSwapIndexComponent o2) {
      return (int) (100 * (o1.getWeight() - o2.getWeight()));
    }
  };

  private CreditDefaultSwapIndexComponent _c1;
  private CreditDefaultSwapIndexComponent _c2;
  private CreditDefaultSwapIndexComponent _c3;
  private CreditDefaultSwapIndexComponent _c4;
  private CreditDefaultSwapIndexComponent _c5;

  /**
   * Sets up the bundle before each method.
   */
  @BeforeTest
  public void setUpInit() {
    _c1 = createComponent("d", "Maroon", 0.05);
    _c2 = createComponent("h", "Green", 0.23);
    _c3 = createComponent("a", "Yellow", 0.01);
    _c4 = createComponent("b", "Blue", 0.17);
    _c5 = createComponent("g", "Grey", 0.09);
  }

  /**
   * Sets up the bundle before each method.
   */
  @BeforeMethod
  public void setUp() {
    _c1 = createComponent("d", "Maroon", 0.05);
    _c2 = createComponent("h", "Green", 0.23);
    _c3 = createComponent("a", "Yellow", 0.01);
    _c4 = createComponent("b", "Blue", 0.17);
    _c5 = createComponent("g", "Grey", 0.09);
  }

  /**
   * Tests that components must be provided.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyBundleIsNotAllowed() {
    CDSIndexComponentBundle.of();
  }


  /**
   * Tests that the comparator cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullComparatorIsNotAllowed() {
    CDSIndexComponentBundle.of(_c1).withCustomIdOrdering(null);
  }

  /**
   * Tests that none of the components can be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoNullsAllowedInComponents() {
    CDSIndexComponentBundle.of(_c1, _c2, null, _c4, _c5);
  }

  /**
   * Tests the size() and isEmpty() method.
   */
  public void testSizeAndIsEmpty() {
    final CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1, _c2, _c3, _c4);
    assertFalse(bundle.isEmpty());
    assertEquals(bundle.size(), 4);
  }

  /**
   * Tests the default ordering of index components.
   */
  @Test
  public void testDefaultElementOrdering() {

    final CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1, _c2, _c3, _c4, _c5);

    assertEquals(ImmutableList.copyOf(bundle.getComponents()),
        ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c4, _c1, _c5, _c2));

  }

  /**
   * Tests that index components are sorted as they are added to the bundle.
   */
  @Test
  public void testElementsAreSortedWhenAdded() {
    final CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1)
        .withCDSIndexComponents(_c2)
        .withCDSIndexComponents(_c3);

    assertEquals(ImmutableList.copyOf(bundle.getComponents()),
        ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c1, _c2));

    final CDSIndexComponentBundle updated = bundle
        .withCDSIndexComponents(_c4)
        .withCDSIndexComponents(_c5);

    assertEquals(ImmutableList.copyOf(updated.getComponents()),
        ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c4, _c1, _c5, _c2));

  }

  /**
   * Tests a custom order of index components.
   */
  @Test
  public void testCustomElementOrdering() {

    final CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1, _c2, _c3, _c4, _c5).withCustomIdOrdering(WEIGHT_COMPARATOR);

    assertEquals(ImmutableList.copyOf(bundle.getComponents()),
        ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c1, _c5, _c4, _c2));

  }

  /**
   * Tests that index components are sorted as they are added to the bundle.
   */
  @Test
  public void testElementsAreSortedWhenAddedToSortedBundle() {

    final CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1, _c2, _c3).withCustomIdOrdering(WEIGHT_COMPARATOR);

    assertEquals(ImmutableList.copyOf(bundle.getComponents()),
        ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c1, _c2));

    final CDSIndexComponentBundle updated = bundle
        .withCDSIndexComponents(_c4)
        .withCDSIndexComponents(_c5);

    assertEquals(ImmutableList.copyOf(updated.getComponents()),
        ImmutableList.<CreditDefaultSwapIndexComponent>of(_c3, _c1, _c5, _c4, _c2));

  }

  /**
   * Tests that the bundle can be updated with new index components.
   */
  @Test
  public void testUpdatingComponentIsPossible() {

    final CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1);

    // New component has same red code so should act as an update, not a new insertion
    final CreditDefaultSwapIndexComponent c = createComponent("d", "Purple", 0.15);

    final CDSIndexComponentBundle updated = bundle.withCDSIndexComponents(c);

    final Iterable<CreditDefaultSwapIndexComponent> components = updated.getComponents();
    assertEquals(ImmutableList.copyOf(components),
        ImmutableList.<CreditDefaultSwapIndexComponent>of(c));
  }

  /**
   * Tests that the bundle can be updated with new index components.
   */
  @Test
  public void testUpdatingAndInsertingComponentIsPossible() {

    final CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1);

    // New component has same red code so should act as an update, not a new insertion
    final CreditDefaultSwapIndexComponent c1 = createComponent("d", "Purple", 0.15);
    final CreditDefaultSwapIndexComponent c2 = createComponent("b", "Brown", 0.25);

    final CDSIndexComponentBundle updated = bundle.withCDSIndexComponents(c1, c2);

    final Iterable<CreditDefaultSwapIndexComponent> components = updated.getComponents();
    assertEquals(ImmutableList.copyOf(components),
        ImmutableList.<CreditDefaultSwapIndexComponent>of(c2, c1));
  }

  /**
   * Tests that the bundle can be updated with new index components.
   */
  @Test
  public void testUpdatingSameComponentIsPossible() {

    final CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1);

    // New component has same red code so should act as an update, not a new insertion
    final CreditDefaultSwapIndexComponent c1 = createComponent("d", "Purple", 0.15);
    // But this is also an update to the same
    final CreditDefaultSwapIndexComponent c2 = createComponent("d", "Lilac", 0.25);

    final CDSIndexComponentBundle updated = bundle.withCDSIndexComponents(c1, c2);

    final Iterable<CreditDefaultSwapIndexComponent> components = updated.getComponents();
    assertEquals(ImmutableList.copyOf(components),
        ImmutableList.<CreditDefaultSwapIndexComponent>of(c2));
  }

  /**
   * Tests that adding the same component is equivalent to updating the bundle.
   */
  @Test
  public void testCreatingSameComponentIsPossible() {

    // New component has same red code so should act as an update, not a new insertion
    final CreditDefaultSwapIndexComponent c1 = createComponent("d", "Purple", 0.15);

    final CDSIndexComponentBundle bundle = CDSIndexComponentBundle.of(_c1, c1);

    final Iterable<CreditDefaultSwapIndexComponent> components = bundle.getComponents();
    assertEquals(ImmutableList.copyOf(components),
        ImmutableList.<CreditDefaultSwapIndexComponent>of(c1));
  }


  /**
   * Creates an index component.
   *
   * @param red
   *          the red code
   * @param name
   *          the name
   * @param weight
   *          the weight
   * @return the component
   */
  private static CreditDefaultSwapIndexComponent createComponent(final String red, final String name, final double weight) {
    return new CreditDefaultSwapIndexComponent(name, redCode(red), weight, null);
  }

  /**
   * Returns a red code identifier.
   *
   * @param red
   *          the code
   * @return the identifier
   */
  private static ExternalId redCode(final String red) {
    return ExternalSchemes.markItRedCode(red);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    final SortedSet<CreditDefaultSwapIndexComponent> values = new TreeSet<>(WEIGHT_COMPARATOR);
    values.add(_c1);
    values.add(_c2);
    final SortedSet<CreditDefaultSwapIndexComponent> otherValues = new TreeSet<>(WEIGHT_COMPARATOR);
    otherValues.add(_c3);
    otherValues.add(_c4);
    return new JodaBeanProperties<>(CDSIndexComponentBundle.class, Arrays.asList("components"), Arrays.asList(values), Arrays.asList(otherValues));
  }

}