/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;

/**
 * Tests for {@link VolatilitySurfaceKey}.
 */
public class VolatilitySurfaceKeyTest {
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
    final MutableFudgeMsg msg = KEY.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()));
    assertEquals(VolatilitySurfaceKey.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg), KEY);
    // tests old conversion
    msg.remove("target");
    msg.add("currency", "USD");
    assertEquals(VolatilitySurfaceKey.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg),
        VolatilitySurfaceKey.of(Currency.USD, NAME, INSTRUMENT_TYPE, QUOTE_TYPE, QUOTE_UNITS));
  }

  /**
   * Tests the visitor.
   */
  @Test
  public void testVisitor() {
    assertEquals(KEY.accept(TestKeyVisitor.INSTANCE), "VolatilitySurfaceKey");
  }
}
