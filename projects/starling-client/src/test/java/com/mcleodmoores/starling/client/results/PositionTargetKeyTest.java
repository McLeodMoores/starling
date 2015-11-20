package com.mcleodmoores.starling.client.results;

import com.opengamma.id.ExternalId;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.swing.text.Position;

import static org.testng.Assert.*;

/**
 * Created by jim on 19/06/15.
 */
public class PositionTargetKeyTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOf() throws Exception {
    PositionTargetKey.of(null);
  }
  @Test
  public void testOf() throws Exception {
    Assert.assertNotNull(PositionTargetKey.of(ExternalId.of("A", "B")));
  }

  @Test
  public void testGetCorrelationId() throws Exception {
    Assert.assertEquals(PositionTargetKey.of(ExternalId.of("A", "B")).getCorrelationId(), ExternalId.of("A", "B"));
  }

  @Test
  public void testHashCode() throws Exception {
    Assert.assertEquals(PositionTargetKey.of(ExternalId.of("A", "B")).hashCode(), PositionTargetKey.of(ExternalId.of("A", "B")).hashCode());
    Assert.assertEquals(PositionTargetKey.of(ExternalId.of("C", "D")).hashCode(), PositionTargetKey.of(ExternalId.of("C", "D")).hashCode());
  }

  @Test
  public void testEquals() throws Exception {
    Assert.assertEquals(PositionTargetKey.of(ExternalId.of("A", "B")), PositionTargetKey.of(ExternalId.of("A", "B")));
    Assert.assertEquals(PositionTargetKey.of(ExternalId.of("C", "D")), PositionTargetKey.of(ExternalId.of("C", "D")));
    Assert.assertNotEquals(PositionTargetKey.of(ExternalId.of("A", "B")), PositionTargetKey.of(ExternalId.of("C", "D")));
    Assert.assertNotEquals(PositionTargetKey.of(ExternalId.of("A", "B")), PositionTargetKey.of(ExternalId.of("A", "D")));
    Assert.assertNotEquals(PositionTargetKey.of(ExternalId.of("A", "B")), PositionTargetKey.of(ExternalId.of("C", "B")));
    Assert.assertNotEquals(PositionTargetKey.of(ExternalId.of("A", "B")), new Object());
    Assert.assertNotEquals(PositionTargetKey.of(ExternalId.of("A", "B")), null);
  }
}