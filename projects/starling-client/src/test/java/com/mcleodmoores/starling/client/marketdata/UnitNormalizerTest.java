package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.id.ExternalIdBundle;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jim on 08/06/15.
 */
public class UnitNormalizerTest {

  @Test
  public void testNormalize() throws Exception {
    Assert.assertEquals(UnitNormalizer.INSTANCE.normalize(ExternalIdBundle.EMPTY, DataField.PRICE, DataSource.DEFAULT, DataProvider.DEFAULT, 20d), 20d);
    Assert.assertEquals(UnitNormalizer.INSTANCE.normalize(ExternalIdBundle.EMPTY, DataField.PRICE, DataSource.DEFAULT, DataProvider.DEFAULT, null), null);
    Assert.assertEquals(UnitNormalizer.INSTANCE.normalize(null, null, null, null, 20d), 20d);
    Assert.assertEquals(UnitNormalizer.INSTANCE.normalize(null, null, null, null, null), null);
  }

  @Test
  public void testGetName() throws Exception {
    Assert.assertEquals("UnitNormalizer", UnitNormalizer.INSTANCE.getName());
  }

  @Test
  public void testToString() throws Exception {
    Assert.assertEquals("UnitNormalizer", UnitNormalizer.INSTANCE.toString());
  }
}