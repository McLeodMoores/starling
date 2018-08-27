/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Month;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Utility class used by WebResource testcases
 */
public final class WebResourceTestUtils {

  public static final ContainerFactory SORTED_JSON_OBJECT_FACTORY = new ContainerFactory() {

    @SuppressWarnings("rawtypes")
    @Override
    public List creatArrayContainer() {
      return new LinkedList();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map createObjectContainer() {
      return new TreeMap();
    }
  };

  private WebResourceTestUtils() {
  }

  /**
   * Returns AAPL equity security for testing
   *
   * @return the equity security
   */
  public static EquitySecurity getEquitySecurity() {
    final EquitySecurity equitySecurity = new EquitySecurity("NASDAQ/NGS (GLOBAL SELECT MARKET)", "XNGS", "APPLE INC", Currency.USD);
    equitySecurity.addExternalId(ExternalSchemes.bloombergTickerSecurityId("AAPL US Equity"));
    equitySecurity.addExternalId(ExternalSchemes.bloombergBuidSecurityId("EQ0010169500001000"));
    equitySecurity.addExternalId(ExternalSchemes.cusipSecurityId("037833100"));
    equitySecurity.addExternalId(ExternalSchemes.isinSecurityId("US0378331005"));
    equitySecurity.addExternalId(ExternalSchemes.sedol1SecurityId("2046251"));
    equitySecurity.setShortName("AAPL");
    equitySecurity.setName("APPLE INC");
    equitySecurity.setGicsCode(GICSCode.of("45202010"));
    return equitySecurity;
  }

  /**
   * Get US bond future security for testing
   *
   * @return the bond future security
   */
  public static BondFutureSecurity getBondFutureSecurity() {
    final Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(2010, Month.JUNE, 21, 19, 0),
        ZoneOffset.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    final Set<BondFutureDeliverable> basket = new HashSet<>();
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810EV6")), 1.0858));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810FB9")), 1.0132));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810PX0")), 0.7984));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810FG8")), 0.9169));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810QD3")), 0.7771));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810FF0")), 0.9174));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810PW2")), 0.7825));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810FE3")), 0.9454));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810QH4")), 0.7757));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810PU6")), 0.8675));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810EX2")), 1.0765));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810FT0")), 0.8054));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810FJ2")), 1.0141));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810PT9")), 0.8352));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810QE1")), 0.8109));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810FP8")), 0.9268));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810QA9")), 0.6606));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810FM5")), 1.0286));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810EY0")), 1.0513));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810QB7")), 0.7616));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810QC5")), 0.795));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810EZ7")), 1.0649));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810EW4")), 1.0));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        ExternalSchemes.bloombergTickerSecurityId("GV912810FA1")), 1.0396));

    final BondFutureSecurity sec = new BondFutureSecurity(expiry, "XCBT", "XCBT", Currency.USD, 1000, basket,
                                                    LocalDate.of(2010, 6, 01).atStartOfDay(ZoneOffset.UTC),
                                                    LocalDate.of(2010, 6, 01).atStartOfDay(ZoneOffset.UTC),
                                                    "Bond");
    sec.setName("US LONG BOND(CBT) Jun10");
    final Set<ExternalId> identifiers = new HashSet<>();
    identifiers.add(ExternalSchemes.bloombergBuidSecurityId("IX8530684-0"));
    identifiers.add(ExternalSchemes.cusipSecurityId("USM10"));
    identifiers.add(ExternalSchemes.bloombergTickerSecurityId("USM10 Comdty"));
    sec.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    return sec;
  }

  public static JSONObject loadJson(final String filePath) throws IOException, JSONException, URISyntaxException {
    final URL jsonResource = ClassLoader.getSystemResource(filePath);
    assertNotNull(jsonResource);
    final String jsonText = FileUtils.readFileToString(new File(jsonResource.toURI()));
    return new JSONObject(jsonText);
  }

  @SuppressWarnings("rawtypes")
  public static void assertJSONObjectEquals(final JSONObject expectedJson, final JSONObject actualJson) throws Exception {
    assertNotNull(expectedJson);
    assertNotNull(actualJson);
    final String expectedSorted = JSONValue.toJSONString(new JSONParser().parse(expectedJson.toString(), SORTED_JSON_OBJECT_FACTORY));
    final String actualSorted = JSONValue.toJSONString(new JSONParser().parse(actualJson.toString(), SORTED_JSON_OBJECT_FACTORY));
    assertEquals(expectedSorted, actualSorted);
  }

}
