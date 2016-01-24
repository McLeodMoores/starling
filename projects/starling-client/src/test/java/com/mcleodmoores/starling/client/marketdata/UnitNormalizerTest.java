/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;

/**
 * Unit tests for {@link UnitNormalizer}.
 */
public class UnitNormalizerTest {

  /**
   * Tests normalization.
   */
  @Test
  public void testNormalize()  {
    assertEquals(UnitNormalizer.INSTANCE.normalize(ExternalIdBundle.EMPTY, DataField.PRICE, DataSource.DEFAULT, DataProvider.DEFAULT, 20d), 20d);
    assertEquals(UnitNormalizer.INSTANCE.normalize(ExternalIdBundle.EMPTY, DataField.PRICE, DataSource.DEFAULT, DataProvider.DEFAULT, null), null);
    assertEquals(UnitNormalizer.INSTANCE.normalize(null, null, null, null, 20d), 20d);
    assertEquals(UnitNormalizer.INSTANCE.normalize(null, null, null, null, null), null);
  }

  /**
   * Tests the name.
   */
  @Test
  public void testGetName()  {
    assertEquals("UnitNormalizer", UnitNormalizer.INSTANCE.getName());
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString()  {
    assertEquals("UnitNormalizer", UnitNormalizer.INSTANCE.toString());
  }

  /**
   * Tests the equals and hashcode methods.
   */
  @Test
  public void testHashcodeEquals() {
    assertEquals(UnitNormalizer.INSTANCE.hashCode(), 1);
    assertEquals(UnitNormalizer.INSTANCE, UnitNormalizer.INSTANCE);
    assertNotEquals(null, UnitNormalizer.INSTANCE);
    assertNotEquals(new Object(), UnitNormalizer.INSTANCE);
    // copy of the existing code
    final Normalizer normalizer = new Normalizer() {

      @Override
      public String getName() {
        return UnitNormalizer.INSTANCE.getName();
      }

      @Override
      public Object normalize(final ExternalIdBundle idBundle, final DataField field, final DataSource source, final DataProvider provider, final Object value) {
        return UnitNormalizer.INSTANCE.normalize(idBundle, field, source, provider, value);
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
        return 1;
      }

    };
    assertNotEquals(UnitNormalizer.INSTANCE, normalizer);
  }

}