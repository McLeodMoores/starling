/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link VolatilitySurfaceKey}.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceKeyTest extends AbstractFudgeBuilderTestCase {
  private static final UniqueIdentifiable TARGET = UniqueId.of("test", "1");
  private static final String NAME = "surface";
  private static final String INSTRUMENT_TYPE = "fx";
  private static final String QUOTE_TYPE = "delta";
  private static final String QUOTE_UNITS = "bps";
  private static final VolatilitySurfaceKey KEY = VolatilitySurfaceKey.of(TARGET, NAME, INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS);

  /**
   * Tests that the target cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTarget() {
    VolatilitySurfaceKey.of(null, NAME, INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS);
  }

  /**
   * Tests the comparator with a null input.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNull() {
    KEY.compareTo(null);
  }

  /**
   * Tests the comparator.
   */
  @Test
  public void testComparator() {
    assertEquals(KEY.compareTo(KEY), 0);
    VolatilitySurfaceKey key = VolatilitySurfaceKey.of(UniqueId.of("test", "2"), NAME, INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS);
    assertTrue(KEY.compareTo(key) < 0);
    assertTrue(key.compareTo(KEY) > 0);
    key = VolatilitySurfaceKey.of(TARGET, "z" + NAME, INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS);
    assertTrue(KEY.compareTo(key) < 0);
    assertTrue(key.compareTo(KEY) > 0);
    key = VolatilitySurfaceKey.of(TARGET, NAME, "z" + INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS);
    assertTrue(KEY.compareTo(key) < 0);
    assertTrue(key.compareTo(KEY) > 0);
    key = VolatilitySurfaceKey.of(TARGET, NAME, INSTRUMENT_TYPE, "z" + QUOTE_TYPE, QUOTE_UNITS);
    assertTrue(KEY.compareTo(key) < 0);
    assertTrue(key.compareTo(KEY) > 0);
    key = VolatilitySurfaceKey.of(TARGET, NAME, INSTRUMENT_TYPE, QUOTE_TYPE, "z" + QUOTE_UNITS);
    assertTrue(KEY.compareTo(key) < 0);
    assertTrue(key.compareTo(KEY) > 0);
  }

  /**
   * Tests an encoding / decoding cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(VolatilitySurfaceKey.class, KEY);
    final MutableFudgeMsg msg = VolatilitySurfaceKeyFudgeBuilder.INSTANCE.buildMessage(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), KEY);
    // tests old conversion
    msg.remove("target");
    msg.add("currency", "USD");
    assertEquals(VolatilitySurfaceKeyFudgeBuilder.INSTANCE.buildObject(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg),
        VolatilitySurfaceKey.of(Currency.USD, NAME, INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS));
  }

  /**
   * Tests the visitor.
   */
  @Test
  public void testVisitor() {
    assertEquals(KEY.accept(TestKeyVisitor.INSTANCE), "VolatilitySurfaceKey");
  }

  /**
   * Tests the hashCode method.
   */
  @Test
  public void testHashCode() {
    assertEquals(KEY.hashCode(), VolatilitySurfaceKey.of(TARGET, NAME, INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final VolatilitySurfaceKey key = VolatilitySurfaceKey.of(TARGET, NAME, INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS);
    assertEquals(key, key);
    assertNotEquals(null, key);
    assertNotEquals(key, NAME);
    VolatilitySurfaceKey other = VolatilitySurfaceKey.of(TARGET, NAME, INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS);
    assertEquals(key, other);
    other = VolatilitySurfaceKey.of(Currency.BRL.getUniqueId(), NAME, INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS);
    assertNotEquals(key, other);
    other  = VolatilitySurfaceKey.of(TARGET, INSTRUMENT_TYPE, INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS);
    assertNotEquals(key, other);
    other  = VolatilitySurfaceKey.of(TARGET, NAME, NAME, QUOTE_TYPE, QUOTE_UNITS);
    assertNotEquals(key, other);
    other  = VolatilitySurfaceKey.of(TARGET, NAME, INSTRUMENT_TYPE, NAME, QUOTE_UNITS);
    assertNotEquals(key, other);
    other  = VolatilitySurfaceKey.of(TARGET, NAME, INSTRUMENT_TYPE, QUOTE_TYPE, NAME);
    assertNotEquals(key, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(KEY.metaBean());
    assertNotNull(KEY.metaBean().instrumentType());
    assertNotNull(KEY.metaBean().name());
    assertNotNull(KEY.metaBean().quoteType());
    assertNotNull(KEY.metaBean().quoteUnits());
    assertNotNull(KEY.metaBean().target());
    assertEquals(KEY.metaBean().instrumentType().get(KEY), INSTRUMENT_TYPE);
    assertEquals(KEY.metaBean().name().get(KEY), NAME);
    assertEquals(KEY.metaBean().quoteType().get(KEY), QUOTE_TYPE);
    assertEquals(KEY.metaBean().quoteUnits().get(KEY), QUOTE_UNITS);
    assertEquals(KEY.metaBean().target().get(KEY), TARGET);
    assertEquals(KEY.property("instrumentType").get(), INSTRUMENT_TYPE);
    assertEquals(KEY.property("name").get(), NAME);
    assertEquals(KEY.property("quoteType").get(), QUOTE_TYPE);
    assertEquals(KEY.property("quoteUnits").get(), QUOTE_UNITS);
    assertEquals(KEY.property("target").get(), TARGET);
  }

}
