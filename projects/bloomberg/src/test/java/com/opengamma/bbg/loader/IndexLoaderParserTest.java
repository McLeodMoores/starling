/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT, singleThreaded = true)
public class IndexLoaderParserTest {

  @Test
  public void testMonths() {
    assertEquals(Tenor.THREE_MONTHS, IndexLoader.decodeTenor("ICE LIBOR USD 3 Months"));
  }
  
  @Test
  public void testOvernight() {
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR Overnight Index"));
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR OVERNIGHT INDEX"));
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR O/N INDEX"));
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR Overnight"));
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR OVERNIGHT"));
    assertEquals(Tenor.ON, IndexLoader.decodeTenor("EURIBOR O/N"));
  }
  
  @Test
  public void testShortMonths() {
    assertEquals(Tenor.SIX_MONTHS, IndexLoader.decodeTenor("EURIBOR 6M"));
  }
  
  @Test
  public void testTomNext() {
    assertEquals(Tenor.TN, IndexLoader.decodeTenor("RANDOM INDEX Tomorrow Next"));
    assertEquals(Tenor.TN, IndexLoader.decodeTenor("RANDOM INDEX Tomorrow/Next"));
    assertEquals(Tenor.TN, IndexLoader.decodeTenor("RANDOM INDEX TOM/NEXT"));
    assertEquals(Tenor.TN, IndexLoader.decodeTenor("RANDOM INDEX T/N TRAILING"));
  }
  
  @Test
  public void testYears() {
    assertEquals(Tenor.THREE_YEARS, IndexLoader.decodeTenor("RANDOM INDEX 3 YEARS"));
  }
  
  @Test
  public void testDays() {
    assertEquals(Tenor.THREE_DAYS, IndexLoader.decodeTenor("RANDOM INDEX 3 Days"));
  }
  
  @Test
  public void testShortDays() {
    assertEquals(Tenor.THREE_DAYS, IndexLoader.decodeTenor("RANDOM INDEX 3 Days"));
  }
  
  @Test
  public void testTurkey() {
    assertEquals(Tenor.THREE_MONTHS, IndexLoader.decodeTenor("Bk Assn of Turkey Interbank 3M"));
  }  

}
