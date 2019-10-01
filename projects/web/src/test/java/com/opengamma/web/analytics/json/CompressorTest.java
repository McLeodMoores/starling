/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.json;

import static org.testng.AssertJUnit.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
// main() method prevents placing "unit" annotation here
public class CompressorTest {

  private static final String JSON = "["
      + "{\"baz\":0.9866,\"foo\":0.9154,\"bar\":[0.10325,0.74086,0.17289]},"
      + "{\"baz\":0.7032,\"foo\":0.53177,\"bar\":[0.40283,0.71098,0.3727,0.61233,0.95098,0.40499]},"
      + "{\"baz\":0.48626,\"foo\":0.81458,\"bar\":[0.29502,0.72687,0.17658,0.03358,0.70506]},"
      + "{\"baz\":0.02826,\"foo\":0.17644,\"bar\":[0.55885,0.15773,0.28012,0.28148]},"
      + "{\"baz\":0.45569,\"foo\":0.97938,\"bar\":[0.9911,0.07979,0.51428,0.502,0.77578,0.56551,0.04773]}]";

  @Test(groups = TestGroup.UNIT)
  public void roundTrip() throws IOException {
    final InputStream source1 = new ByteArrayInputStream(JSON.getBytes());
    final ByteArrayOutputStream sink1 = new ByteArrayOutputStream();
    Compressor.compressStream(source1, sink1);

    final InputStream source2 = new ByteArrayInputStream(sink1.toByteArray());
    final ByteArrayOutputStream sink2 = new ByteArrayOutputStream();
    Compressor.decompressStream(source2, sink2);
    assertEquals(JSON, sink2.toString());
  }

  private static List<Object> randomObjects() {
    final Random random = new Random();
    final List<Object> list = Lists.newArrayList();
    final int size = random.nextInt(5) + 3;
    for (int i = 0; i < size; i++) {
      final Map<String, Object> map = Maps.newHashMap();
      map.put("foo", randomNumber());
      map.put("bar", randomArray());
      map.put("baz", randomNumber());
      list.add(map);
    }
    return list;
  }

  private static BigDecimal randomNumber() {
    return new BigDecimal(Math.random()).setScale(5, RoundingMode.DOWN);
  }

  private static BigDecimal[] randomArray() {
    final Random random = new Random();
    final int size = random.nextInt(3) + 3;
    final BigDecimal[] array = new BigDecimal[size];
    for (int i = 0; i < size; i++) {
      array[i] = randomNumber();
    }
    return array;
  }

  public static void main(final String[] args) throws IOException {
    final InputStream source1 = new ByteArrayInputStream(new JSONArray(randomObjects()).toString().getBytes());
    final ByteArrayOutputStream sink1 = new ByteArrayOutputStream();
    System.out.println(JSON);
    Compressor.compressStream(source1, sink1);
    System.out.println("JSON size: " + JSON.length());
    System.out.println("compressed size: " + sink1.size());
    System.out.println("ratio: " + (double) sink1.size() / (double) JSON.length());
  }
}
