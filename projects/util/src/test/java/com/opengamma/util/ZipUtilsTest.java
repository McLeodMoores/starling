/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ZipUtilsTest {

  @DataProvider(name = "compressString")
  Object[][] data_compressString() {
    return new Object[][] {
        {""},
        {"A"},
        {"0"},
        {"Joda"},
        {"Etienne"},
        {"This is a much longer piece of text"},
        {"<text>This is a longer piece of <i>text</i> that is surrounded by <i>XML</i> tags</text>"},
        {"<person><forename>Stephen</forename><surname>Colebourne</surname><address>Park Street</address><city>London</city></person>"},
        {StringUtils.repeat("<person>Stephen</person>", 100)},
    };
  }

  @Test(dataProvider = "compressString")
  public void test_zipString(final String input) {
    final byte[] bytes = ZipUtils.zipString(input);
    final byte[] expected = ZipUtils.zipString(input, false);
    assertEquals(expected, bytes);
  }

  @Test(dataProvider = "compressString")
  public void test_zipString_optimize(final String input) {
    final byte[] bytes = ZipUtils.zipString(input, true);
    debug(input, bytes);
    final String str = ZipUtils.unzipString(bytes);
    assertEquals(str, input);
  }

  @Test(dataProvider = "compressString")
  public void test_zipString_noOptimize(final String input) {
    final byte[] bytes = ZipUtils.zipString(input, false);
    debug(input, bytes);
    final String str = ZipUtils.unzipString(bytes);
    assertEquals(str, input);
  }

  @Test(dataProvider = "compressString")
  public void test_deflateString_inflateString(final String input) {
    final byte[] bytes = ZipUtils.deflateString(input);
    debug(input, bytes);
    final String str = ZipUtils.inflateString(bytes);
    assertEquals(str, input);
  }

  private void debug(final String input, final byte[] bytes) {
//    System.out.println(bytes.length + " " + input.length() + " " + Arrays.toString(bytes) + " " + input);
  }

}
