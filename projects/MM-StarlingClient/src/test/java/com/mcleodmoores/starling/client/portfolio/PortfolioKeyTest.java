package com.mcleodmoores.starling.client.portfolio;

import com.opengamma.id.UniqueId;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by jim on 10/06/15.
 */
@Test
public class PortfolioKeyTest {

  @Test
  public void testOf() throws Exception {
    PortfolioKey key = PortfolioKey.of("TEST");
    Assert.assertNotNull(key);
    Assert.assertEquals(key.getName(), "TEST");
    Assert.assertNull(key.getUniqueId());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfNull() throws Exception {
    PortfolioKey.of(null);
  }

  @Test
  public void testOf1() throws Exception {
    PortfolioKey key = PortfolioKey.of("TEST", UniqueId.of("A", "B"));
    Assert.assertNotNull(key);
    Assert.assertEquals(key.getName(), "TEST");
    Assert.assertEquals(key.getUniqueId(), UniqueId.of("A", "B"));
  }

  @Test
  public void testOf1Null() {
    PortfolioKey.of("OK", null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOf1Null1() throws Exception {
    PortfolioKey.of(null, null);
  }

  @Test
  public void testGetUniqueId() throws Exception {
    Assert.assertEquals(PortfolioKey.of("Hello", UniqueId.of("A", "B")).getUniqueId(), UniqueId.of("A", "B"));
  }

  @Test
  public void testHasUniqueId() throws Exception {
    Assert.assertTrue(PortfolioKey.of("Hello", UniqueId.of("A", "B")).hasUniqueId());
    Assert.assertFalse(PortfolioKey.of("Hello").hasUniqueId());
  }

  @Test
  public void testGetName() throws Exception {
    Assert.assertEquals(PortfolioKey.of("Hello", UniqueId.of("A", "B")).getName(), "Hello");
  }

  @Test
  public void testHashCode() throws Exception {
    Assert.assertEquals(PortfolioKey.of("Hello").hashCode(), PortfolioKey.of("Hello").hashCode());
    Assert.assertEquals(PortfolioKey.of("Goodbye").hashCode(), PortfolioKey.of("Goodbye").hashCode());
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")).hashCode(), PortfolioKey.of("Goodbye").hashCode());
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")).hashCode(), PortfolioKey.of("Goodbye", UniqueId.of("A", "B")).hashCode());
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")).hashCode(), PortfolioKey.of("Goodbye", UniqueId.of("A", "C")).hashCode());
  }

  @Test
  public void testEquals() throws Exception {
    Assert.assertEquals(PortfolioKey.of("Hello"), PortfolioKey.of("Hello"));
    Assert.assertEquals(PortfolioKey.of("Goodbye"), PortfolioKey.of("Goodbye"));
    Assert.assertNotEquals(PortfolioKey.of("Hello"), PortfolioKey.of("Goodbye"));
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")), PortfolioKey.of("Goodbye"));
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")), PortfolioKey.of("Goodbye", UniqueId.of("A", "B")));
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")), PortfolioKey.of("Goodbye", UniqueId.of("A", "C")));
    Assert.assertNotEquals(PortfolioKey.of("Hello", UniqueId.of("A", "B")), PortfolioKey.of("Goodbye", UniqueId.of("A", "B")));
  }

  @Test
  public void testToString() throws Exception {
    Assert.assertEquals("PortfolioKey[Goodbye(A~B)]", PortfolioKey.of("Goodbye", UniqueId.of("A", "B")).toString());
    Assert.assertEquals("PortfolioKey[Goodbye]", PortfolioKey.of("Goodbye").toString());
  }
}