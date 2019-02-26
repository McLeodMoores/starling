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

  /**
   * @return data to be compressed
   */
  @DataProvider(name = "compressString")
  Object[][] dataCompressString() {
    return new Object[][] { { "" }, { "A" }, { "0" }, { "Joda" }, { "Etienne" }, { "This is a much longer piece of text" },
        { "<text>This is a longer piece of <i>text</i> that is surrounded by <i>XML</i> tags</text>" },
        { "<person><forename>Stephen</forename><surname>Colebourne</surname><address>Park Street</address><city>London</city></person>" },
        { StringUtils.repeat("<person>Stephen</person>", 100) }, };
  }

  /**
   * @param input
   *          the input
   */
  @Test(dataProvider = "compressString")
  public void testZipString(final String input) {
    final byte[] bytes = ZipUtils.zipString(input);
    final byte[] expected = ZipUtils.zipString(input, false);
    assertEquals(expected, bytes);
  }

  /**
   * @param input
   *          the input
   */
  @Test(dataProvider = "compressString")
  public void testZipStringOptimize(final String input) {
    final byte[] bytes = ZipUtils.zipString(input, true);
    final String str = ZipUtils.unzipString(bytes);
    assertEquals(str, input);
  }

  /**
   * @param input
   *          the input
   */
  @Test(dataProvider = "compressString")
  public void testZipStringNoOptimize(final String input) {
    final byte[] bytes = ZipUtils.zipString(input, false);
    final String str = ZipUtils.unzipString(bytes);
    assertEquals(str, input);
  }

  /**
   * @param input
   *          the input
   */
  @Test(dataProvider = "compressString")
  public void testDeflateStringInflateString(final String input) {
    final byte[] bytes = ZipUtils.deflateString(input);
    final String str = ZipUtils.inflateString(bytes);
    assertEquals(str, input);
  }

}
