/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.util.test.TestGroup;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Unit tests for {@link QuandlOvernightIndexConventionsLoader}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlOvernightIndexConventionsLoaderTest {

  /**
   * This tests that the expected csv file exists, that there is a header that is ignored and the expected number of conventions are created. Individual
   * conventions are not tested as this file could change.
   *
   * @throws Exception
   *           if there is a problem reading the file
   */
  @Test
  public void test() throws Exception {
    final QuandlOvernightIndexConventionsLoader loader = QuandlOvernightIndexConventionsLoader.INSTANCE;
    try (InputStream resource = getClass().getResourceAsStream("overnight-index-conventions.csv")) {
      if (resource == null) {
        fail("Could not open overnight-index-conventions.csv");
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        int lineCount = 0;
        while (reader.readNext() != null) {
          lineCount++;
        }
        if (lineCount == 0) {
          fail("overnight-index-conventions.csv was empty");
        }
        final Set<OvernightIndexConvention> conventions = loader.loadConventionsFromFile();
        // expect there to be a header that is ignored when creating conventions
        assertEquals(conventions.size(), lineCount - 1);
      }
    }

  }
}
