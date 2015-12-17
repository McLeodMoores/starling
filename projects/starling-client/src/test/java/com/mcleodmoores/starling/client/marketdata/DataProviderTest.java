package com.mcleodmoores.starling.client.marketdata;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Created by jim on 08/06/15.
 */
@Test(groups = TestGroup.UNIT)
public class DataProviderTest {
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    DataField.of(null);
  }

  public void testNormalOperation() {
    Assert.assertNotNull(DataProvider.of("ICPL"));
    Assert.assertEquals(DataProvider.of("ICPL").getName(), "ICPL");
  }

  public void testPooling() {
    Assert.assertTrue(DataProvider.of("ICPL") == DataProvider.of("ICPL"));
    Assert.assertFalse(DataProvider.of("ICPL") == DataProvider.of("LAST_ICPL"));
  }

  public void testEquals() {
    Assert.assertEquals(DataProvider.of("ICPL"), DataProvider.of("ICPL"));
    Assert.assertEquals(DataProvider.of(""), DataProvider.of(""));
    Assert.assertEquals(DataProvider.of("BCAL"), DataProvider.of("BCAL"));
    Assert.assertNotEquals(DataProvider.of("ICPL"), DataProvider.of("BCAL"));
    Assert.assertNotEquals(DataProvider.of("BCAL"), DataProvider.of("ICPL"));
    Assert.assertNotEquals(DataProvider.of("BCAL"), null);
    Assert.assertNotEquals(DataProvider.of("BCAL"), new Object());
  }

  public void testHashcode() {
    Assert.assertEquals(DataProvider.of("ICPL").hashCode(), DataProvider.of("ICPL").hashCode());
    Assert.assertEquals(DataProvider.of("BCAL").hashCode(), DataProvider.of("BCAL").hashCode());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryNull() {
    DataProviderFactory.INSTANCE.of(null);
  }

  public void testFactory() {
    Assert.assertEquals(DataProvider.of("ICPL"), DataProviderFactory.INSTANCE.of("ICPL"));
    Assert.assertEquals(DataProviderFactory.INSTANCE.of("ICPL"), DataProvider.of("ICPL"));
  }

  public void testToString() {
    Assert.assertEquals(DataProvider.of("ICPL").toString(), "ICPL");
  }

}