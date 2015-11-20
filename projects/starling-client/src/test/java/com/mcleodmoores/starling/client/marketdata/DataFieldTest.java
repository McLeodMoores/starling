package com.mcleodmoores.starling.client.marketdata;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jim on 08/06/15.
 */
@Test
public class DataFieldTest {
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    DataField.of(null);
  }

  public void testNormalOperation() {
    Assert.assertNotNull(DataField.of("PRICE"));
    Assert.assertEquals(DataField.of("PRICE").getName(), "PRICE");
  }

  public void testPooling() {
    Assert.assertTrue(DataField.of("PRICE") == DataField.of("PRICE"));
    Assert.assertFalse(DataField.of("PRICE") == DataField.of("LAST_PRICE"));
  }

  public void testEquals() {
    Assert.assertEquals(DataField.of("PRICE"), DataField.of("PRICE"));
    Assert.assertEquals(DataField.of(""), DataField.of(""));
    Assert.assertEquals(DataField.of("PRICE2"), DataField.of("PRICE2"));
    Assert.assertNotEquals(DataField.of("PRICE"), DataField.of("PRICE2"));
    Assert.assertNotEquals(DataField.of("PRICE2"), DataField.of("PRICE"));
    Assert.assertNotEquals(DataField.of("PRICE2"), null);
    Assert.assertNotEquals(DataField.of("PRICE2"), new Object());
  }

  public void testHashcode() {
    Assert.assertEquals(DataField.of("PRICE").hashCode(), DataField.of("PRICE").hashCode());
    Assert.assertEquals(DataField.of("PRICE2").hashCode(), DataField.of("PRICE2").hashCode());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryNull() {
    DataFieldFactory.INSTANCE.of(null);
  }

  public void testFactory() {
    Assert.assertEquals(DataField.of("PRICE"), DataFieldFactory.INSTANCE.of("PRICE"));
    Assert.assertEquals(DataFieldFactory.INSTANCE.of("PRICE"), DataField.of("PRICE"));
  }

  public void testToString() {
    Assert.assertEquals(DataField.of("ICPL").toString(), "ICPL");
  }

}