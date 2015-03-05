/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test Currency.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyFactoryTest {

  //-----------------------------------------------------------------------
  // constants
  //-----------------------------------------------------------------------
  public void test_constants() {
    assertEquals(Currency.USD, CurrencyFactory.INSTANCE.instance("USD"));
    assertEquals(Currency.EUR, CurrencyFactory.INSTANCE.instance("EUR"));
    assertEquals(Currency.JPY, CurrencyFactory.INSTANCE.instance("JPY"));
    assertEquals(Currency.GBP, CurrencyFactory.INSTANCE.instance("GBP"));
    assertEquals(Currency.CHF, CurrencyFactory.INSTANCE.instance("CHF"));
    assertEquals(Currency.AUD, CurrencyFactory.INSTANCE.instance("AUD"));
    assertEquals(Currency.CAD, CurrencyFactory.INSTANCE.instance("CAD"));
  }

  //-----------------------------------------------------------------------
  // instanceMap()
  //-----------------------------------------------------------------------
  public void test_getAvailable() {
    Map<String, Currency> instanceMap = CurrencyFactory.INSTANCE.instanceMap();
    for (Map.Entry<String, Currency> entry : instanceMap.entrySet()) {
      assertEquals(entry.getKey(), entry.getValue().getCode());
    }
  }

  //-----------------------------------------------------------------------
  // instance(String)
  //-----------------------------------------------------------------------
  public void test_of_Currency() {
    Currency test = CurrencyFactory.INSTANCE.instance("GBP");
    assertEquals("GBP", test.getCode());
    assertSame(Currency.GBP, CurrencyFactory.INSTANCE.instance("GBP"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_Currency_nullString() {
    CurrencyFactory.INSTANCE.instance((String) null);
  }

//  @Test(expectedExceptions = IllegalArgumentException.class)
//  public void test_of_String_lowerCase() {
//    try {
//      CurrencyFactory.INSTANCE.instance("gbp");
//    } catch (IllegalArgumentException ex) {
//      assertEquals("Invalid currency code: gbp", ex.getMessage());
//      throw ex;
//    }
//  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_empty() {
    CurrencyFactory.INSTANCE.instance("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_tooShort() {
    CurrencyFactory.INSTANCE.instance("AB");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_of_String_tooLong() {
    CurrencyFactory.INSTANCE.instance("ABCD");
  }

  //-----------------------------------------------------------------------
  // Serialisation
  //-----------------------------------------------------------------------
  public void test_serialization_GBP() throws Exception {
    Currency cu = CurrencyFactory.INSTANCE.instance("GBP");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(cu);
    oos.close();
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    Currency input = (Currency) ois.readObject();
    assertSame(input, cu);
  }

  //-----------------------------------------------------------------------
  // gets
  //-----------------------------------------------------------------------
  public void test_gets() {
    Currency test = CurrencyFactory.INSTANCE.instance("GBP");
    assertEquals("GBP", test.getCode());
    assertEquals(ObjectId.of("CurrencyISO", "GBP"), test.getObjectId());
    assertEquals(UniqueId.of("CurrencyISO", "GBP"), test.getUniqueId());
    assertEquals(java.util.Currency.getInstance("GBP"), test.toCurrency());
  }
}
