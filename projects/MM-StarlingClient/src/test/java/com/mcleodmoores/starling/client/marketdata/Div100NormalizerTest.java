package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.id.ExternalIdBundle;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jim on 08/06/15.
 */
public class Div100NormalizerTest {
  @Test
  public void testNormalize() throws Exception {
    Assert.assertEquals(Div100Normalizer.INSTANCE.normalize(ExternalIdBundle.EMPTY, DataField.PRICE, DataSource.DEFAULT, DataProvider.DEFAULT, 20d), 0.2d);
    Assert.assertEquals(Div100Normalizer.INSTANCE.normalize(ExternalIdBundle.EMPTY, DataField.PRICE, DataSource.DEFAULT, DataProvider.DEFAULT, null), null);
    Assert.assertEquals(Div100Normalizer.INSTANCE.normalize(null, null, null, null, 20d), 0.2d);
    Assert.assertEquals(Div100Normalizer.INSTANCE.normalize(null, null, null, null, null), null);
  }

  @Test
  public void testGetName() throws Exception {
    Assert.assertEquals("Div100Normalizer", Div100Normalizer.INSTANCE.getName());
  }

  @Test
  public void testToString() throws Exception {
    Assert.assertEquals("Div100Normalizer", Div100Normalizer.INSTANCE.toString());
  }
}