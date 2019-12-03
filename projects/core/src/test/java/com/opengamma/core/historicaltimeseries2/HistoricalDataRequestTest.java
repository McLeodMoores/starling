/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.historicaltimeseries2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link HistoricalDataRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalDataRequestTest extends AbstractFudgeBuilderTestCase {
  private static final ExternalIdBundle EID = ExternalIdBundle.of("eid", "100");
  private static final String FIELD = MarketDataRequirementNames.ANNUAL_DIVIDEND;
  private static final LocalDate FROM = LocalDate.of(2016, 9, 18);
  private static final Boolean FROM_INCLUSIVE = false;
  private static final LocalDate TO = LocalDate.of(2018, 9, 18);
  private static final Boolean TO_INCLUSIVE = true;
  private static final String RESOLVER = "resolver";

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final HistoricalDataRequest hdr = HistoricalDataRequest.builder()
        .bundle(EID).field(FIELD).from(FROM).fromInclusive(FROM_INCLUSIVE).to(TO).toInclusive(TO_INCLUSIVE).resolver(RESOLVER).build();
    assertEquals(hdr, hdr);
    assertNotEquals(null, hdr);
    assertNotEquals(EID, hdr);
    assertEquals(hdr.toString(), "HistoricalDataRequest{bundle=Bundle[eid~100], field=Market_AnnualDividend, "
        + "from=2016-09-18, fromInclusive=false, to=2018-09-18, toInclusive=true, resolver=resolver}");
    HistoricalDataRequest other = HistoricalDataRequest.builder()
        .bundle(EID).field(FIELD).from(FROM).fromInclusive(FROM_INCLUSIVE).to(TO).toInclusive(TO_INCLUSIVE).resolver(RESOLVER).build();
    assertEquals(hdr, other);
    assertEquals(hdr.hashCode(), other.hashCode());
    other = HistoricalDataRequest.builder()
        .bundle(ExternalIdBundle.of("eid", "200")).field(FIELD).from(FROM).fromInclusive(FROM_INCLUSIVE).to(TO)
        .toInclusive(TO_INCLUSIVE).resolver(RESOLVER).build();
    assertNotEquals(hdr, other);
    other = HistoricalDataRequest.builder()
        .bundle(EID).field("field").from(FROM).fromInclusive(FROM_INCLUSIVE).to(TO).toInclusive(TO_INCLUSIVE).resolver(RESOLVER).build();
    assertNotEquals(hdr, other);
    other = HistoricalDataRequest.builder()
        .bundle(EID).field(FIELD).from(TO).fromInclusive(FROM_INCLUSIVE).to(TO).toInclusive(TO_INCLUSIVE).resolver(RESOLVER).build();
    assertNotEquals(hdr, other);
    other = HistoricalDataRequest.builder()
        .bundle(EID).field(FIELD).from(FROM).fromInclusive(TO_INCLUSIVE).to(TO).toInclusive(TO_INCLUSIVE).resolver(RESOLVER).build();
    assertNotEquals(hdr, other);
    other = HistoricalDataRequest.builder()
        .bundle(EID).field(FIELD).from(FROM).fromInclusive(FROM_INCLUSIVE).to(FROM).toInclusive(TO_INCLUSIVE).resolver(RESOLVER).build();
    assertNotEquals(hdr, other);
    other = HistoricalDataRequest.builder()
        .bundle(EID).field(FIELD).from(FROM).fromInclusive(FROM_INCLUSIVE).to(TO).toInclusive(FROM_INCLUSIVE).resolver(RESOLVER).build();
    assertNotEquals(hdr, other);
    other = HistoricalDataRequest.builder()
        .bundle(EID).field(FIELD).from(FROM).fromInclusive(FROM_INCLUSIVE).to(TO).toInclusive(TO_INCLUSIVE).resolver("r").build();
    assertNotEquals(hdr, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final HistoricalDataRequest hdr = HistoricalDataRequest.builder()
        .bundle(EID).field(FIELD).from(FROM).fromInclusive(FROM_INCLUSIVE).to(TO).toInclusive(TO_INCLUSIVE).resolver(RESOLVER).build();
    assertEquals(hdr.metaBean().bundle().get(hdr), EID);
    assertEquals(hdr.metaBean().field().get(hdr), FIELD);
    assertEquals(hdr.metaBean().from().get(hdr), FROM);
    assertEquals(hdr.metaBean().fromInclusive().get(hdr), FROM_INCLUSIVE);
    assertEquals(hdr.metaBean().to().get(hdr), TO);
    assertEquals(hdr.metaBean().toInclusive().get(hdr), TO_INCLUSIVE);
    assertEquals(hdr.metaBean().resolver().get(hdr), RESOLVER);
    assertEquals(hdr.property("bundle").get(), EID);
    assertEquals(hdr.property("field").get(), FIELD);
    assertEquals(hdr.property("from").get(), FROM);
    assertEquals(hdr.property("fromInclusive").get(), FROM_INCLUSIVE);
    assertEquals(hdr.property("to").get(), TO);
    assertEquals(hdr.property("toInclusive").get(), TO_INCLUSIVE);
    assertEquals(hdr.property("resolver").get(), RESOLVER);
  }

  /**
   * Tests the cycle.
   */
  @Test
  public void testCycle() {
    final HistoricalDataRequest hdr = HistoricalDataRequest.builder()
        .bundle(EID).field(FIELD).from(FROM).fromInclusive(FROM_INCLUSIVE).to(TO).toInclusive(TO_INCLUSIVE).resolver(RESOLVER).build();
    assertEncodeDecodeCycle(HistoricalDataRequest.class, hdr);
  }
}
