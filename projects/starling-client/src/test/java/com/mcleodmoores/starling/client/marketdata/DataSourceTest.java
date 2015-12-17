package com.mcleodmoores.starling.client.marketdata;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Created by jim on 08/06/15.
 */
@Test(groups = TestGroup.UNIT)
public class DataSourceTest {
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    DataField.of(null);
  }

  public void testNormalOperation() {
    Assert.assertNotNull(DataSource.of("QUANDL"));
    Assert.assertEquals(DataSource.of("QUANDL").getName(), "QUANDL");
  }

  public void testPooling() {
    Assert.assertTrue(DataSource.of("QUANDL") == DataSource.of("QUANDL"));
    Assert.assertFalse(DataSource.of("QUANDL") == DataSource.of("LAST_QUANDL"));
  }

  public void testEquals() {
    Assert.assertEquals(DataSource.of("QUANDL"), DataSource.of("QUANDL"));
    Assert.assertEquals(DataSource.of(""), DataSource.of(""));
    Assert.assertEquals(DataSource.of("BLOOMBERG"), DataSource.of("BLOOMBERG"));
    Assert.assertNotEquals(DataSource.of("QUANDL"), DataSource.of("BLOOMBERG"));
    Assert.assertNotEquals(DataSource.of("BLOOMBERG"), DataSource.of("QUANDL"));
    Assert.assertNotEquals(DataSource.of("BLOOMBERG"), null);
    Assert.assertNotEquals(DataSource.of("BLOOMBERG"), new Object());
  }

  public void testHashcode() {
    Assert.assertEquals(DataSource.of("QUANDL").hashCode(), DataSource.of("QUANDL").hashCode());
    Assert.assertEquals(DataSource.of("BLOOMBERG").hashCode(), DataSource.of("BLOOMBERG").hashCode());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryNull() {
    DataSourceFactory.INSTANCE.of(null);
  }

  public void testFactory() {
    Assert.assertEquals(DataSource.of("QUANDL"), DataSourceFactory.INSTANCE.of("QUANDL"));
    Assert.assertEquals(DataSourceFactory.INSTANCE.of("QUANDL"), DataSource.of("QUANDL"));
  }

  public void testToString() {
    Assert.assertEquals(DataSource.of("QUANDL").toString(), "QUANDL");
  }
}