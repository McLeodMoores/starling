/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import com.google.common.collect.Sets;
import com.opengamma.bbg.livedata.LoggedReferenceDataProvider;
import com.opengamma.bbg.util.MockReferenceDataProvider;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergRefDataCollectorTest {

  private static final String WATCH_LIST_FILE = "watchListTest.txt";
  private static final String FIELD_LIST_FILE = "fieldListTest.txt";
  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  private BloombergRefDataCollector _refDataCollector;
  private File _outputFile;

  @BeforeMethod
  public void setUp(final Method m) throws Exception {
    final MockReferenceDataProvider refDataProvider = new MockReferenceDataProvider();
    refDataProvider.addExpectedField("SECURITY_TYP");
    refDataProvider.addResult("QQQQ US Equity", "SECURITY_TYP", "ETP");
    refDataProvider.addResult("/buid/EQ0082335400001000", "SECURITY_TYP", "ETP");

    final File watchListFile = new File(BloombergRefDataCollectorTest.class.getResource(WATCH_LIST_FILE).toURI());
    final File fieldListFile = new File(BloombergRefDataCollectorTest.class.getResource(FIELD_LIST_FILE).toURI());

    final String outfileName = getClass().getSimpleName() + "-" + Thread.currentThread().getName() +
        "-" + OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));

    _outputFile = File.createTempFile(outfileName, null);
    _outputFile.deleteOnExit();

    _refDataCollector = new BloombergRefDataCollector(FUDGE_CONTEXT, watchListFile, refDataProvider, fieldListFile, _outputFile);
    _refDataCollector.start();
  }

  @AfterMethod
  public void tearDown() throws Exception {
    //clean up
    _refDataCollector.stop();
  }

  //-------------------------------------------------------------------------
  @Test
  public void test() {
    final LoggedReferenceDataProvider loggedRefDataProvider = new LoggedReferenceDataProvider(FUDGE_CONTEXT, _outputFile);

    final Set<String> securities = Sets.newHashSet("QQQQ US Equity", "/buid/EQ0082335400001000");
    final Set<String> fields = Collections.singleton("SECURITY_TYP");
    final Map<String, FudgeMsg> refDataMap = loggedRefDataProvider.getReferenceData(securities, fields);

    final Set<String> testSecurities = refDataMap.keySet();
    assertEquals(2, testSecurities.size());

    assertTrue(testSecurities.containsAll(securities));

    for (final String security : testSecurities) {
      final FudgeMsg fieldData = refDataMap.get(security);
      final String securityType = fieldData.getString("SECURITY_TYP");
      assertEquals("ETP", securityType);
    }
  }

}
