/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link Div100Normalizer}.
 */
@Test(groups = TestGroup.UNIT)
public class Div100NormalizerTest {

  /**
   * Tests the normalization.
   */
  @Test
  public void testNormalize() {
    assertEquals(Div100Normalizer.INSTANCE.normalize(ExternalIdBundle.EMPTY, DataField.PRICE, DataSource.DEFAULT, DataProvider.DEFAULT, 20d), 0.2d);
    assertEquals(Div100Normalizer.INSTANCE.normalize(ExternalIdBundle.EMPTY, DataField.PRICE, DataSource.DEFAULT, DataProvider.DEFAULT, null), null);
    assertEquals(Div100Normalizer.INSTANCE.normalize(null, null, null, null, 20d), 0.2d);
    assertEquals(Div100Normalizer.INSTANCE.normalize(null, null, null, null, null), null);
  }

  /**
   * Tests the equals and hashcode methods.
   */
  @Test
  public void testHashcodeEquals() {
    assertEquals(Div100Normalizer.INSTANCE.hashCode(), 100);
    assertEquals(Div100Normalizer.INSTANCE, Div100Normalizer.INSTANCE);
    assertNotEquals(null, Div100Normalizer.INSTANCE);
    assertNotEquals(new Object(), Div100Normalizer.INSTANCE);
    // copy of the existing code
    final Normalizer normalizer = new Normalizer() {

      @Override
      public String getName() {
        return Div100Normalizer.INSTANCE.getName();
      }

      @Override
      public Object normalize(final ExternalIdBundle idBundle, final DataField field, final DataSource source, final DataProvider provider, 
          final Object value) {
        return Div100Normalizer.INSTANCE.normalize(idBundle, field, source, provider, value);
      }

      @Override
      public String toString() {
        return getName();
      }

      @Override
      public boolean equals(final Object other) {
        if (other == null) {
          return false;
        }
        if (other == this) {
          return true;
        }
        return false;
      }

      @Override
      public int hashCode() {
        return 100;
      }

    };
    assertNotEquals(Div100Normalizer.INSTANCE, normalizer);
  }

  /**
   * Tests the name.
   */
  @Test
  public void testGetName() {
    assertEquals("Div100Normalizer", Div100Normalizer.INSTANCE.getName());
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    assertEquals("Div100Normalizer", Div100Normalizer.INSTANCE.toString());
  }
}