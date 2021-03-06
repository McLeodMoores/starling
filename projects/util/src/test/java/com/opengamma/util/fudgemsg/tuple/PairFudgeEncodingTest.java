/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.tuple;

import static org.testng.Assert.assertEquals;

import org.fudgemsg.FudgeMsg;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.IntDoublePair;
import com.opengamma.util.tuple.IntObjectPair;
import com.opengamma.util.tuple.LongDoublePair;
import com.opengamma.util.tuple.LongObjectPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class PairFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   *
   */
  public void testOOBundle() {
    final Pair<String, ExternalIdBundle> object = ObjectsPair.of("Hello", ExternalIdBundle.of(ExternalId.of("A", "B")));
    assertEncodeDecodeCycle(Pair.class, object);
  }

  /**
   *
   */
  public void testOOUniqueId() {
    final Pair<String, UniqueId> object = ObjectsPair.of("Hello", UniqueId.of("A", "B"));
    assertEncodeDecodeCycle(Pair.class, object);
  }

  /**
   *
   */
  public void testOONull() {
    final Pair<String, UniqueId> object = ObjectsPair.of("Hello", null);
    assertEncodeDecodeCycle(Pair.class, object);
  }

  /**
   *
   */
  public void testLO() {
    final Pair<Long, UniqueId> object = LongObjectPair.of(23L, UniqueId.of("A", "B"));
    assertEncodeDecodeCycle(Pair.class, object);
  }

  /**
   *
   */
  public void testLD() {
    final Pair<Long, Double> object = LongDoublePair.of(23L, 4.5d);
    assertEncodeDecodeCycle(Pair.class, object);
  }

  /**
   *
   */
  public void testIO() {
    final Pair<Integer, UniqueId> object = IntObjectPair.of(23, UniqueId.of("A", "B"));
    assertEncodeDecodeCycle(Pair.class, object);
  }

  /**
   *
   */
  public void testID() {
    final Pair<Integer, Double> object = IntDoublePair.of(23, 4.5d);
    assertEncodeDecodeCycle(Pair.class, object);
  }

  /**
   *
   */
  public void testDD() {
    final Pair<Double, Double> object = DoublesPair.of(23.2, 4.5d);
    assertEncodeDecodeCycle(Pair.class, object);
  }

  /**
   *
   */
  public void testTypeWithSecondaryTypeAndBuilderEncoding() {
    final Pair<Tenor, Tenor> object = ObjectsPair.of(Tenor.DAY, Tenor.TWELVE_MONTHS);
    assertEncodeDecodeCycle(Pair.class, object);
  }

  /**
   *
   */
  public void teststaticTypedMethods() {
    final ObjectsPair<Tenor, Tenor> in = ObjectsPair.of(Tenor.DAY, Tenor.TEN_YEARS);
    FudgeMsg msg = ObjectsPairFudgeBuilder.buildMessage(getFudgeSerializer(), in, Tenor.class, Tenor.class);
    ObjectsPair<Tenor, Tenor> out = ObjectsPairFudgeBuilder.buildObject(getFudgeDeserializer(), msg, Tenor.class, Tenor.class);
    assertEquals(out, in);
    msg = cycleMessage(msg);
    out = ObjectsPairFudgeBuilder.buildObject(getFudgeDeserializer(), msg, Tenor.class, Tenor.class);
    assertEquals(out, in);
  }

  /**
   *
   */
  public void testTypeWithSecondaryTypeAndReducedNumber() {
    final ObjectsPair<LocalDate, Long> object = ObjectsPair.of(LocalDate.of(2011, 6, 30), 6L);
    assertEncodeDecodeCycle(Pair.class, object);
  }

  /**
   *
   */
  public void testNullFirst() {
    final ObjectsPair<String, String> object = ObjectsPair.of(null, "B");
    assertEncodeDecodeCycle(Pair.class, object);
  }

  /**
   *
   */
  public void testNullSecond() {
    final ObjectsPair<String, String> object = ObjectsPair.of("A", null);
    assertEncodeDecodeCycle(Pair.class, object);
  }

}
